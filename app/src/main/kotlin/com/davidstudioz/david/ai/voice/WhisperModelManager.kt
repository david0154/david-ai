package com.davidstudioz.david.ai.voice

import android.content.Context
import com.davidstudioz.david.core.model.ModelLifecycleManager
import com.davidstudioz.david.core.model.ModelLoader
import com.davidstudioz.david.core.model.ModelType
import com.davidstudioz.david.core.model.ModelValidator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced Whisper Model Manager with:
 * - GPU acceleration (TFLite GPU delegate)
 * - NNAPI support for compatible devices
 * - Memory-mapped model loading
 * - INT8 quantization support
 * - Model warming on background thread
 * - Proper error boundaries
 */
@Singleton
class WhisperModelManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lifecycleManager: ModelLifecycleManager,
    private val validator: ModelValidator
) : ModelLoader {

    companion object {
        private const val TAG = "WhisperModelManager"
        private const val MODEL_FILENAME = "whisper_base_int8.tflite"
        private const val SAMPLE_RATE = 16000
        private const val MEL_BINS = 80
        private const val MAX_AUDIO_LENGTH_SECONDS = 30
    }

    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    private var nnApiDelegate: NnApiDelegate? = null
    private var isModelLoaded = false
    private var isGpuSupported = false
    private var isNnApiSupported = false

    init {
        // Register with lifecycle manager
        lifecycleManager.registerModelLoader(ModelType.WHISPER, this)
        checkHardwareAcceleration()
    }

    /**
     * Check hardware acceleration support
     */
    private fun checkHardwareAcceleration() {
        // Check GPU support
        val compatibilityList = CompatibilityList()
        isGpuSupported = compatibilityList.isDelegateSupportedOnThisDevice

        // NNAPI is supported on Android 8.1+ (API 27+)
        isNnApiSupported = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1
    }

    /**
     * Load the Whisper model
     */
    override suspend fun load(): Result<Any> = withContext(Dispatchers.IO) {
        try {
            if (isModelLoaded) {
                return@withContext Result.success(interpreter!!)
            }

            val modelFile = getModelFile()
            
            // Validate model before loading
            val validationResult = validator.validateModel(modelFile, performLoadTest = false)
            if (validationResult.isFailed()) {
                return@withContext Result.failure(
                    Exception("Model validation failed: ${validationResult.getErrorOrNull()?.message}")
                )
            }

            // Load model with optimal settings
            val modelBuffer = loadModelFile(modelFile)
            val options = createInterpreterOptions()
            
            interpreter = Interpreter(modelBuffer, options)
            
            // Warm up the model
            warmUpModel()
            
            isModelLoaded = true
            Result.success(interpreter!!)

        } catch (e: Exception) {
            cleanup()
            Result.failure(e)
        }
    }

    /**
     * Unload the model and free resources
     */
    override suspend fun unload() = withContext(Dispatchers.IO) {
        cleanup()
    }

    /**
     * Transcribe audio to text
     */
    suspend fun transcribe(audioData: FloatArray): Result<String> = withContext(Dispatchers.Default) {
        try {
            // Ensure model is loaded
            if (!isModelLoaded) {
                val loadResult = lifecycleManager.loadModel(ModelType.WHISPER)
                if (loadResult.isFailure) {
                    return@withContext Result.failure(
                        loadResult.exceptionOrNull() ?: Exception("Failed to load Whisper model")
                    )
                }
            }

            val currentInterpreter = interpreter
                ?: return@withContext Result.failure(Exception("Model not loaded"))

            // Preprocess audio
            val melSpectrogram = preprocessAudio(audioData)

            // Prepare input tensor
            val inputBuffer = ByteBuffer.allocateDirect(melSpectrogram.size * 4).apply {
                order(ByteOrder.nativeOrder())
                melSpectrogram.forEach { putFloat(it) }
                rewind()
            }

            // Prepare output tensors
            val outputShape = currentInterpreter.getOutputTensor(0).shape()
            val outputBuffer = ByteBuffer.allocateDirect(outputShape.fold(1) { acc, i -> acc * i } * 4).apply {
                order(ByteOrder.nativeOrder())
            }

            // Run inference
            currentInterpreter.run(inputBuffer, outputBuffer)

            // Post-process output to text
            val transcription = postprocessOutput(outputBuffer, outputShape)

            Result.success(transcription)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Transcribe audio stream (real-time)
     */
    suspend fun transcribeStream(
        audioChunks: List<FloatArray>,
        onPartialResult: (String) -> Unit
    ): Result<String> = withContext(Dispatchers.Default) {
        try {
            val fullTranscription = StringBuilder()

            for ((index, chunk) in audioChunks.withIndex()) {
                val result = transcribe(chunk)
                
                if (result.isSuccess) {
                    val partial = result.getOrNull() ?: ""
                    fullTranscription.append(partial).append(" ")
                    onPartialResult(partial)
                } else {
                    return@withContext result
                }
            }

            Result.success(fullTranscription.toString().trim())

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create interpreter options with hardware acceleration
     */
    private fun createInterpreterOptions(): Interpreter.Options {
        return Interpreter.Options().apply {
            // Set number of threads based on CPU cores
            val numCores = Runtime.getRuntime().availableProcessors()
            setNumThreads(minOf(numCores, 4))

            // Try GPU acceleration first
            if (isGpuSupported) {
                try {
                    gpuDelegate = GpuDelegate()
                    addDelegate(gpuDelegate)
                    android.util.Log.d(TAG, "GPU acceleration enabled")
                } catch (e: Exception) {
                    android.util.Log.w(TAG, "GPU acceleration failed, falling back: ${e.message}")
                    gpuDelegate = null
                }
            }

            // Try NNAPI if GPU failed or not supported
            if (gpuDelegate == null && isNnApiSupported) {
                try {
                    nnApiDelegate = NnApiDelegate()
                    addDelegate(nnApiDelegate)
                    android.util.Log.d(TAG, "NNAPI acceleration enabled")
                } catch (e: Exception) {
                    android.util.Log.w(TAG, "NNAPI acceleration failed: ${e.message}")
                    nnApiDelegate = null
                }
            }

            // Allow FP16 precision for GPU
            if (gpuDelegate != null) {
                setAllowFp16PrecisionForFp32(true)
            }

            // Use memory-mapped model for efficiency
            setUseXNNPACK(true)
        }
    }

    /**
     * Load model file using memory mapping
     */
    private fun loadModelFile(modelFile: File): MappedByteBuffer {
        FileInputStream(modelFile).use { inputStream ->
            val fileChannel = inputStream.channel
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size())
        }
    }

    /**
     * Warm up the model with dummy input
     */
    private fun warmUpModel() {
        try {
            val dummyInput = FloatArray(SAMPLE_RATE * 5) { 0f } // 5 seconds of silence
            val dummyMel = preprocessAudio(dummyInput)
            
            val inputBuffer = ByteBuffer.allocateDirect(dummyMel.size * 4).apply {
                order(ByteOrder.nativeOrder())
                dummyMel.forEach { putFloat(it) }
                rewind()
            }

            val outputShape = interpreter?.getOutputTensor(0)?.shape() ?: return
            val outputBuffer = ByteBuffer.allocateDirect(outputShape.fold(1) { acc, i -> acc * i } * 4).apply {
                order(ByteOrder.nativeOrder())
            }

            interpreter?.run(inputBuffer, outputBuffer)
            android.util.Log.d(TAG, "Model warmed up successfully")

        } catch (e: Exception) {
            android.util.Log.w(TAG, "Model warm-up failed: ${e.message}")
        }
    }

    /**
     * Preprocess audio to mel spectrogram
     */
    private fun preprocessAudio(audioData: FloatArray): FloatArray {
        // Trim or pad audio to max length
        val maxLength = SAMPLE_RATE * MAX_AUDIO_LENGTH_SECONDS
        val processedAudio = when {
            audioData.size > maxLength -> audioData.copyOf(maxLength)
            audioData.size < maxLength -> audioData + FloatArray(maxLength - audioData.size) { 0f }
            else -> audioData
        }

        // Normalize audio
        val normalizedAudio = normalizeAudio(processedAudio)

        // Convert to mel spectrogram
        return computeMelSpectrogram(normalizedAudio)
    }

    /**
     * Normalize audio data
     */
    private fun normalizeAudio(audio: FloatArray): FloatArray {
        val max = audio.maxOrNull()?.takeIf { it > 0f } ?: 1f
        return audio.map { it / max }.toFloatArray()
    }

    /**
     * Compute mel spectrogram from audio
     * Simplified version - in production, use a proper audio processing library
     */
    private fun computeMelSpectrogram(audio: FloatArray): FloatArray {
        // This is a simplified placeholder
        // In production, implement proper STFT + Mel filterbank
        // Or use existing libraries like TarsosDSP
        
        val frameSize = 400 // 25ms at 16kHz
        val hopSize = 160 // 10ms at 16kHz
        val numFrames = (audio.size - frameSize) / hopSize + 1
        
        val melSpec = FloatArray(numFrames * MEL_BINS)
        
        // Simplified mel spectrogram computation
        for (i in 0 until numFrames) {
            val start = i * hopSize
            val frame = audio.sliceArray(start until minOf(start + frameSize, audio.size))
            
            // Compute energy per mel bin (simplified)
            for (bin in 0 until MEL_BINS) {
                val energy = frame.sumOf { it.toDouble() * it.toDouble() }.toFloat()
                melSpec[i * MEL_BINS + bin] = kotlin.math.log10(energy + 1e-10f)
            }
        }
        
        return melSpec
    }

    /**
     * Post-process model output to text
     */
    private fun postprocessOutput(outputBuffer: ByteBuffer, outputShape: IntArray): String {
        outputBuffer.rewind()
        
        // Extract token IDs from output buffer
        val tokenCount = outputShape.fold(1) { acc, i -> acc * i }
        val tokens = IntArray(tokenCount) {
            outputBuffer.getInt()
        }

        // Decode tokens to text (simplified)
        // In production, use proper tokenizer/decoder
        return decodeTokens(tokens)
    }

    /**
     * Decode token IDs to text
     * Placeholder - should use proper Whisper tokenizer
     */
    private fun decodeTokens(tokens: IntArray): String {
        // This is a placeholder
        // In production, implement proper Whisper token decoding
        // or integrate with the tokenizer from the model
        return tokens.joinToString(" ") { it.toString() }
    }

    /**
     * Get model file from storage
     */
    private fun getModelFile(): File {
        // Check in files directory first
        val filesDir = File(context.filesDir, "models")
        val modelFile = File(filesDir, MODEL_FILENAME)
        
        if (modelFile.exists()) {
            return modelFile
        }

        // Check in assets as fallback
        val assetsModelFile = File(context.cacheDir, MODEL_FILENAME)
        if (!assetsModelFile.exists()) {
            // Copy from assets
            context.assets.open(MODEL_FILENAME).use { input ->
                assetsModelFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        return assetsModelFile
    }

    /**
     * Cleanup resources
     */
    private fun cleanup() {
        interpreter?.close()
        interpreter = null
        
        gpuDelegate?.close()
        gpuDelegate = null
        
        nnApiDelegate?.close()
        nnApiDelegate = null
        
        isModelLoaded = false
    }

    /**
     * Check if model is loaded
     */
    fun isLoaded(): Boolean = isModelLoaded

    /**
     * Get model info
     */
    fun getModelInfo(): WhisperModelInfo {
        val interpreter = this.interpreter
        return if (interpreter != null && isModelLoaded) {
            WhisperModelInfo(
                isLoaded = true,
                inputShape = interpreter.getInputTensor(0).shape().toList(),
                outputShape = interpreter.getOutputTensor(0).shape().toList(),
                isGpuAccelerated = gpuDelegate != null,
                isNnApiAccelerated = nnApiDelegate != null,
                numThreads = interpreter.inputTensorCount
            )
        } else {
            WhisperModelInfo(
                isLoaded = false,
                inputShape = emptyList(),
                outputShape = emptyList(),
                isGpuAccelerated = false,
                isNnApiAccelerated = false,
                numThreads = 0
            )
        }
    }
}

/**
 * Whisper model information
 */
data class WhisperModelInfo(
    val isLoaded: Boolean,
    val inputShape: List<Int>,
    val outputShape: List<Int>,
    val isGpuAccelerated: Boolean,
    val isNnApiAccelerated: Boolean,
    val numThreads: Int
)
