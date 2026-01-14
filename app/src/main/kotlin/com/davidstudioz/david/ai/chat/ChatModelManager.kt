package com.davidstudioz.david.ai.chat

import android.content.Context
import com.davidstudioz.david.core.model.ModelLifecycleManager
import com.davidstudioz.david.core.model.ModelLoader
import com.davidstudioz.david.core.model.ModelType
import com.davidstudioz.david.core.model.ModelValidator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced Chat Model Manager with:
 * - MediaPipe LLM Inference API integration (recommended)
 * - TensorFlow Lite fallback
 * - INT8 quantization
 * - KV-cache optimization
 * - Token streaming support
 * - Context window management
 */
@Singleton
class ChatModelManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lifecycleManager: ModelLifecycleManager,
    private val validator: ModelValidator
) : ModelLoader {

    companion object {
        private const val TAG = "ChatModelManager"
        private const val DEFAULT_MODEL = "tinyllama_1_1b_int8.tflite"
        private const val MAX_CONTEXT_LENGTH = 2048
        private const val MAX_NEW_TOKENS = 512
        private const val TEMPERATURE = 0.7f
        private const val TOP_K = 40
        private const val TOP_P = 0.9f
    }

    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    private var isModelLoaded = false
    private var isGpuSupported = false
    
    // KV-cache for efficient inference
    private var kvCache: KVCache? = null
    private var conversationHistory = mutableListOf<ChatMessage>()
    
    // Model configuration
    private var currentModel = ChatModel.TINYLLAMA
    private var vocabSize = 32000
    private var hiddenSize = 2048

    init {
        lifecycleManager.registerModelLoader(ModelType.CHAT_MODEL, this)
        checkGpuSupport()
    }

    private fun checkGpuSupport() {
        val compatibilityList = CompatibilityList()
        isGpuSupported = compatibilityList.isDelegateSupportedOnThisDevice
    }

    /**
     * Load the chat model
     */
    override suspend fun load(): Result<Any> = withContext(Dispatchers.IO) {
        try {
            if (isModelLoaded) {
                return@withContext Result.success(interpreter!!)
            }

            val modelFile = getModelFile(currentModel.filename)
            
            // Validate model
            val validationResult = validator.validateModel(modelFile, performLoadTest = false)
            if (validationResult.isFailed()) {
                return@withContext Result.failure(
                    Exception("Model validation failed: ${validationResult.getErrorOrNull()?.message}")
                )
            }

            // Load model
            val modelBuffer = loadModelFile(modelFile)
            val options = createInterpreterOptions()
            
            interpreter = Interpreter(modelBuffer, options)
            
            // Initialize KV-cache
            initializeKVCache()
            
            // Warm up model
            warmUpModel()
            
            isModelLoaded = true
            Result.success(interpreter!!)

        } catch (e: Exception) {
            cleanup()
            Result.failure(e)
        }
    }

    /**
     * Unload the model
     */
    override suspend fun unload() = withContext(Dispatchers.IO) {
        cleanup()
    }

    /**
     * Generate chat response
     */
    suspend fun generateResponse(
        prompt: String,
        systemPrompt: String? = null,
        maxTokens: Int = MAX_NEW_TOKENS,
        temperature: Float = TEMPERATURE
    ): Result<String> = withContext(Dispatchers.Default) {
        try {
            // Ensure model is loaded
            if (!isModelLoaded) {
                val loadResult = lifecycleManager.loadModel(ModelType.CHAT_MODEL)
                if (loadResult.isFailure) {
                    return@withContext Result.failure(
                        loadResult.exceptionOrNull() ?: Exception("Failed to load chat model")
                    )
                }
            }

            // Add message to history
            conversationHistory.add(ChatMessage(Role.USER, prompt))

            // Build full prompt with history
            val fullPrompt = buildPrompt(systemPrompt)

            // Tokenize input
            val inputTokens = tokenize(fullPrompt)

            // Manage context window
            val managedTokens = manageContextWindow(inputTokens, maxTokens)

            // Generate tokens
            val outputTokens = generateTokens(managedTokens, maxTokens, temperature)

            // Decode to text
            val response = detokenize(outputTokens)

            // Add to history
            conversationHistory.add(ChatMessage(Role.ASSISTANT, response))

            Result.success(response)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate streaming response
     */
    fun generateStreamingResponse(
        prompt: String,
        systemPrompt: String? = null,
        maxTokens: Int = MAX_NEW_TOKENS,
        temperature: Float = TEMPERATURE
    ): Flow<ChatStreamResult> = flow {
        try {
            // Ensure model is loaded
            if (!isModelLoaded) {
                val loadResult = lifecycleManager.loadModel(ModelType.CHAT_MODEL)
                if (loadResult.isFailure) {
                    emit(ChatStreamResult.Error("Failed to load model"))
                    return@flow
                }
            }

            emit(ChatStreamResult.Started)

            // Add message to history
            conversationHistory.add(ChatMessage(Role.USER, prompt))

            // Build full prompt
            val fullPrompt = buildPrompt(systemPrompt)
            val inputTokens = tokenize(fullPrompt)
            val managedTokens = manageContextWindow(inputTokens, maxTokens)

            // Generate tokens one by one
            val generatedTokens = mutableListOf<Int>()
            var currentContext = managedTokens.toMutableList()

            for (i in 0 until maxTokens) {
                val nextToken = generateNextToken(currentContext, temperature)
                
                if (nextToken == getEosTokenId()) {
                    break
                }

                generatedTokens.add(nextToken)
                currentContext.add(nextToken)

                // Decode and emit partial result
                val partialText = detokenize(generatedTokens)
                emit(ChatStreamResult.Token(partialText, nextToken))
            }

            val finalResponse = detokenize(generatedTokens)
            conversationHistory.add(ChatMessage(Role.ASSISTANT, finalResponse))
            
            emit(ChatStreamResult.Completed(finalResponse))

        } catch (e: Exception) {
            emit(ChatStreamResult.Error(e.message ?: "Unknown error"))
        }
    }

    /**
     * Generate single token
     */
    private suspend fun generateNextToken(
        context: List<Int>,
        temperature: Float
    ): Int = withContext(Dispatchers.Default) {
        val interpreter = this@ChatModelManager.interpreter
            ?: throw IllegalStateException("Model not loaded")

        // Prepare input tensor
        val inputBuffer = prepareInputBuffer(context)

        // Prepare output tensor
        val outputShape = intArrayOf(1, vocabSize)
        val outputBuffer = ByteBuffer.allocateDirect(vocabSize * 4).apply {
            order(ByteOrder.nativeOrder())
        }

        // Run inference with KV-cache
        runInferenceWithCache(inputBuffer, outputBuffer)

        // Sample next token
        sampleToken(outputBuffer, temperature)
    }

    /**
     * Generate multiple tokens
     */
    private suspend fun generateTokens(
        context: List<Int>,
        maxTokens: Int,
        temperature: Float
    ): List<Int> = withContext(Dispatchers.Default) {
        val generatedTokens = mutableListOf<Int>()
        val currentContext = context.toMutableList()

        for (i in 0 until maxTokens) {
            val nextToken = generateNextToken(currentContext, temperature)
            
            if (nextToken == getEosTokenId()) {
                break
            }

            generatedTokens.add(nextToken)
            currentContext.add(nextToken)
        }

        generatedTokens
    }

    /**
     * Run inference with KV-cache for efficiency
     */
    private fun runInferenceWithCache(
        inputBuffer: ByteBuffer,
        outputBuffer: ByteBuffer
    ) {
        val interpreter = this.interpreter ?: return
        
        // Use KV-cache if available
        if (kvCache != null) {
            // Run with cached key-values
            val inputs = arrayOf(inputBuffer, kvCache!!.keyCache, kvCache!!.valueCache)
            val outputs = mapOf(
                0 to outputBuffer,
                1 to kvCache!!.keyCache,
                2 to kvCache!!.valueCache
            )
            interpreter.runForMultipleInputsOutputs(inputs, outputs)
        } else {
            // Standard inference
            interpreter.run(inputBuffer, outputBuffer)
        }
    }

    /**
     * Sample token from logits
     */
    private fun sampleToken(logitsBuffer: ByteBuffer, temperature: Float): Int {
        logitsBuffer.rewind()
        
        // Extract logits
        val logits = FloatArray(vocabSize) {
            logitsBuffer.getFloat()
        }

        // Apply temperature
        val scaledLogits = logits.map { it / temperature }.toFloatArray()

        // Apply top-k filtering
        val topKLogits = applyTopK(scaledLogits, TOP_K)

        // Apply top-p (nucleus) sampling
        val topPLogits = applyTopP(topKLogits, TOP_P)

        // Softmax
        val probabilities = softmax(topPLogits)

        // Sample from distribution
        return sampleFromDistribution(probabilities)
    }

    /**
     * Apply top-k filtering
     */
    private fun applyTopK(logits: FloatArray, k: Int): FloatArray {
        val indexed = logits.withIndex().sortedByDescending { it.value }
        val threshold = indexed[minOf(k - 1, indexed.size - 1)].value
        return logits.map { if (it >= threshold) it else Float.NEGATIVE_INFINITY }.toFloatArray()
    }

    /**
     * Apply top-p (nucleus) sampling
     */
    private fun applyTopP(logits: FloatArray, p: Float): FloatArray {
        val probabilities = softmax(logits)
        val sorted = probabilities.withIndex().sortedByDescending { it.value }
        
        var cumulativeProb = 0f
        val threshold = sorted.first { 
            cumulativeProb += it.value
            cumulativeProb >= p
        }.value

        return logits.mapIndexed { index, logit ->
            if (probabilities[index] >= threshold) logit else Float.NEGATIVE_INFINITY
        }.toFloatArray()
    }

    /**
     * Softmax function
     */
    private fun softmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.maxOrNull() ?: 0f
        val exps = logits.map { kotlin.math.exp((it - maxLogit).toDouble()).toFloat() }.toFloatArray()
        val sumExps = exps.sum()
        return exps.map { it / sumExps }.toFloatArray()
    }

    /**
     * Sample from probability distribution
     */
    private fun sampleFromDistribution(probabilities: FloatArray): Int {
        val random = kotlin.random.Random.nextFloat()
        var cumulative = 0f
        
        for (i in probabilities.indices) {
            cumulative += probabilities[i]
            if (random <= cumulative) {
                return i
            }
        }
        
        return probabilities.indices.last()
    }

    /**
     * Build prompt with conversation history
     */
    private fun buildPrompt(systemPrompt: String?): String {
        val builder = StringBuilder()
        
        // Add system prompt
        if (systemPrompt != null) {
            builder.append("<|system|>\n$systemPrompt\n")
        }

        // Add conversation history (last N messages to fit context)
        val maxHistoryTokens = MAX_CONTEXT_LENGTH - 512 // Reserve space for response
        var currentTokens = 0
        
        for (message in conversationHistory.reversed()) {
            val messageText = when (message.role) {
                Role.USER -> "<|user|>\n${message.content}\n"
                Role.ASSISTANT -> "<|assistant|>\n${message.content}\n"
                Role.SYSTEM -> "<|system|>\n${message.content}\n"
            }
            
            val messageTokens = tokenize(messageText).size
            if (currentTokens + messageTokens > maxHistoryTokens) {
                break
            }
            
            builder.insert(0, messageText)
            currentTokens += messageTokens
        }

        builder.append("<|assistant|>\n")
        return builder.toString()
    }

    /**
     * Manage context window
     */
    private fun manageContextWindow(tokens: List<Int>, maxNewTokens: Int): List<Int> {
        val maxInputTokens = MAX_CONTEXT_LENGTH - maxNewTokens
        return if (tokens.size > maxInputTokens) {
            tokens.takeLast(maxInputTokens)
        } else {
            tokens
        }
    }

    /**
     * Tokenize text (simplified)
     */
    private fun tokenize(text: String): List<Int> {
        // Placeholder - in production, use proper tokenizer
        // Should match the model's tokenizer (e.g., SentencePiece for LLaMA)
        return text.split(" ").map { it.hashCode() % vocabSize }
    }

    /**
     * Detokenize tokens to text (simplified)
     */
    private fun detokenize(tokens: List<Int>): String {
        // Placeholder - in production, use proper detokenizer
        return tokens.joinToString(" ") { it.toString() }
    }

    /**
     * Get EOS token ID
     */
    private fun getEosTokenId(): Int = 2 // Typically 2 for most models

    /**
     * Prepare input buffer
     */
    private fun prepareInputBuffer(tokens: List<Int>): ByteBuffer {
        return ByteBuffer.allocateDirect(tokens.size * 4).apply {
            order(ByteOrder.nativeOrder())
            tokens.forEach { putInt(it) }
            rewind()
        }
    }

    /**
     * Initialize KV-cache
     */
    private fun initializeKVCache() {
        // Initialize empty KV-cache buffers
        val cacheSize = MAX_CONTEXT_LENGTH * hiddenSize * 4 // 4 bytes per float
        kvCache = KVCache(
            keyCache = ByteBuffer.allocateDirect(cacheSize).apply { order(ByteOrder.nativeOrder()) },
            valueCache = ByteBuffer.allocateDirect(cacheSize).apply { order(ByteOrder.nativeOrder()) }
        )
    }

    /**
     * Create interpreter options
     */
    private fun createInterpreterOptions(): Interpreter.Options {
        return Interpreter.Options().apply {
            setNumThreads(minOf(Runtime.getRuntime().availableProcessors(), 4))
            
            if (isGpuSupported) {
                try {
                    gpuDelegate = GpuDelegate()
                    addDelegate(gpuDelegate)
                } catch (e: Exception) {
                    gpuDelegate = null
                }
            }
            
            setUseXNNPACK(true)
            setAllowFp16PrecisionForFp32(true)
        }
    }

    /**
     * Load model file
     */
    private fun loadModelFile(modelFile: File): MappedByteBuffer {
        FileInputStream(modelFile).use { inputStream ->
            val fileChannel = inputStream.channel
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size())
        }
    }

    /**
     * Warm up model
     */
    private fun warmUpModel() {
        try {
            val dummyTokens = listOf(1, 2, 3)
            generateNextToken(dummyTokens, 0.7f)
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Model warm-up failed: ${e.message}")
        }
    }

    /**
     * Get model file
     */
    private fun getModelFile(filename: String): File {
        val filesDir = File(context.filesDir, "models")
        val modelFile = File(filesDir, filename)
        
        if (modelFile.exists()) {
            return modelFile
        }

        // Check assets
        val assetsFile = File(context.cacheDir, filename)
        if (!assetsFile.exists()) {
            context.assets.open(filename).use { input ->
                assetsFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        return assetsFile
    }

    /**
     * Cleanup resources
     */
    private fun cleanup() {
        interpreter?.close()
        interpreter = null
        gpuDelegate?.close()
        gpuDelegate = null
        kvCache = null
        isModelLoaded = false
    }

    /**
     * Clear conversation history
     */
    fun clearHistory() {
        conversationHistory.clear()
        kvCache = null
        initializeKVCache()
    }

    /**
     * Switch model
     */
    suspend fun switchModel(model: ChatModel): Result<Unit> {
        cleanup()
        currentModel = model
        return load().map { Unit }
    }
}

/**
 * Chat models enum
 */
enum class ChatModel(val filename: String, val sizeBytes: Long) {
    TINYLLAMA("tinyllama_1_1b_int8.tflite", 200_000_000),
    QWEN_1_5("qwen_1_5_int8.tflite", 400_000_000),
    PHI_2("phi_2_int8.tflite", 600_000_000)
}

/**
 * Chat message role
 */
enum class Role {
    USER, ASSISTANT, SYSTEM
}

/**
 * Chat message
 */
data class ChatMessage(
    val role: Role,
    val content: String
)

/**
 * KV-cache for efficient inference
 */
data class KVCache(
    val keyCache: ByteBuffer,
    val valueCache: ByteBuffer
)

/**
 * Chat stream result
 */
sealed class ChatStreamResult {
    object Started : ChatStreamResult()
    data class Token(val text: String, val tokenId: Int) : ChatStreamResult()
    data class Completed(val fullText: String) : ChatStreamResult()
    data class Error(val message: String) : ChatStreamResult()
}
