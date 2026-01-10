package com.davidstudioz.david.models

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

data class AIModel(
    val name: String,
    val url: String,
    val size: String,
    val minRamGB: Int,
    val type: String, // "LLM", "Vision", "Speech", "Language", "Gesture"
    val format: String = "GGUF" // "GGUF", "ONNX", "TFLite", "GGML"
)

/**
 * ModelManager - ANDROID-COMPATIBLE MODELS ONLY
 * ✅ GGUF (Chat - llama.cpp)
 * ✅ GGML (Voice - whisper.cpp)
 * ✅ ONNX (Vision - ONNX Runtime)
 * ✅ TFLite (Language, Gesture - TensorFlow Lite)
 * ❌ NO PyTorch (doesn't run on Android)
 */
class ModelManager(private val context: Context) {
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
        
    private val modelsDir = File(context.getExternalFilesDir(null), "models")
    
    init {
        try {
            modelsDir.mkdirs()
            Log.d(TAG, "Models directory: ${modelsDir.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating models directory", e)
        }
    }
    
    /**
     * D.A.V.I.D AI Models - ANDROID-COMPATIBLE ONLY
     */
    private val availableModels = listOf(
        // ===== D.A.V.I.D CHAT MODELS (GGUF - llama.cpp) =====
        AIModel(
            name = "D.A.V.I.D Chat Light",
            url = "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf",
            size = "669 MB",
            minRamGB = 2,
            type = "LLM",
            format = "GGUF"
        ),
        AIModel(
            name = "D.A.V.I.D Chat Standard",
            url = "https://huggingface.co/second-state/Qwen1.5-1.8B-Chat-GGUF/resolve/main/qwen1.5-1.8b-chat-q4_k_m.gguf",
            size = "1.1 GB",
            minRamGB = 2,
            type = "LLM",
            format = "GGUF"
        ),
        AIModel(
            name = "D.A.V.I.D Chat Pro",
            url = "https://huggingface.co/TheBloke/phi-2-GGUF/resolve/main/phi-2.Q4_K_M.gguf",
            size = "1.6 GB",
            minRamGB = 3,
            type = "LLM",
            format = "GGUF"
        ),
        
        // ===== D.A.V.I.D VOICE MODELS (GGML - whisper.cpp) =====
        AIModel(
            name = "D.A.V.I.D Voice Tiny",
            url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.en.bin",
            size = "75 MB",
            minRamGB = 1,
            type = "Speech",
            format = "GGML"
        ),
        AIModel(
            name = "D.A.V.I.D Voice Base",
            url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.en.bin",
            size = "142 MB",
            minRamGB = 1,
            type = "Speech",
            format = "GGML"
        ),
        AIModel(
            name = "D.A.V.I.D Voice Small",
            url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small.en.bin",
            size = "466 MB",
            minRamGB = 2,
            type = "Speech",
            format = "GGML"
        ),
        
        // ===== D.A.V.I.D VISION MODELS (ONNX/TFLite) =====
        AIModel(
            name = "D.A.V.I.D Vision Lite",
            url = "https://github.com/onnx/models/raw/main/validated/vision/classification/mobilenet/model/mobilenetv2-12.onnx",
            size = "14 MB",
            minRamGB = 1,
            type = "Vision",
            format = "ONNX"
        ),
        AIModel(
            name = "D.A.V.I.D Vision Standard",
            url = "https://github.com/onnx/models/raw/main/validated/vision/classification/resnet/model/resnet50-v2-7.onnx",
            size = "98 MB",
            minRamGB = 2,
            type = "Vision",
            format = "ONNX"
        ),
        
        // ===== D.A.V.I.D LANGUAGE MODELS (TFLite - MobileBERT) =====
        // Using MobileBERT which is optimized for mobile devices
        AIModel(
            name = "D.A.V.I.D Language English",
            url = "https://tfhub.dev/tensorflow/lite-model/mobilebert/1/default/1?lite-format=tflite",
            size = "100 MB",
            minRamGB = 1,
            type = "Language",
            format = "TFLite"
        ),
        AIModel(
            name = "D.A.V.I.D Language Multilingual",
            url = "https://tfhub.dev/tensorflow/lite-model/mobilebert/1/default/1?lite-format=tflite",
            size = "100 MB",
            minRamGB = 1,
            type = "Language",
            format = "TFLite"
        ),
        
        // For Indian languages, using Universal Sentence Encoder Multilingual (supports 16 languages including Hindi, Tamil, etc)
        AIModel(
            name = "D.A.V.I.D Language Hindi",
            url = "https://tfhub.dev/google/lite-model/universal-sentence-encoder-multilingual/3?lite-format=tflite",
            size = "50 MB",
            minRamGB = 1,
            type = "Language",
            format = "TFLite"
        ),
        AIModel(
            name = "D.A.V.I.D Language Tamil",
            url = "https://tfhub.dev/google/lite-model/universal-sentence-encoder-multilingual/3?lite-format=tflite",
            size = "50 MB",
            minRamGB = 1,
            type = "Language",
            format = "TFLite"
        ),
        AIModel(
            name = "D.A.V.I.D Language Telugu",
            url = "https://tfhub.dev/google/lite-model/universal-sentence-encoder-multilingual/3?lite-format=tflite",
            size = "50 MB",
            minRamGB = 1,
            type = "Language",
            format = "TFLite"
        ),
        AIModel(
            name = "D.A.V.I.D Language Bengali",
            url = "https://tfhub.dev/google/lite-model/universal-sentence-encoder-multilingual/3?lite-format=tflite",
            size = "50 MB",
            minRamGB = 1,
            type = "Language",
            format = "TFLite"
        ),
        AIModel(
            name = "D.A.V.I.D Language Marathi",
            url = "https://tfhub.dev/google/lite-model/universal-sentence-encoder-multilingual/3?lite-format=tflite",
            size = "50 MB",
            minRamGB = 1,
            type = "Language",
            format = "TFLite"
        ),
        AIModel(
            name = "D.A.V.I.D Language Gujarati",
            url = "https://tfhub.dev/google/lite-model/universal-sentence-encoder-multilingual/3?lite-format=tflite",
            size = "50 MB",
            minRamGB = 1,
            type = "Language",
            format = "TFLite"
        ),
        AIModel(
            name = "D.A.V.I.D Language Kannada",
            url = "https://tfhub.dev/google/lite-model/universal-sentence-encoder-multilingual/3?lite-format=tflite",
            size = "50 MB",
            minRamGB = 1,
            type = "Language",
            format = "TFLite"
        ),
        AIModel(
            name = "D.A.V.I.D Language Malayalam",
            url = "https://tfhub.dev/google/lite-model/universal-sentence-encoder-multilingual/3?lite-format=tflite",
            size = "50 MB",
            minRamGB = 1,
            type = "Language",
            format = "TFLite"
        ),
        AIModel(
            name = "D.A.V.I.D Language Punjabi",
            url = "https://tfhub.dev/google/lite-model/universal-sentence-encoder-multilingual/3?lite-format=tflite",
            size = "50 MB",
            minRamGB = 1,
            type = "Language",
            format = "TFLite"
        ),
        AIModel(
            name = "D.A.V.I.D Language Urdu",
            url = "https://tfhub.dev/google/lite-model/universal-sentence-encoder-multilingual/3?lite-format=tflite",
            size = "50 MB",
            minRamGB = 1,
            type = "Language",
            format = "TFLite"
        ),
        
        // ===== D.A.V.I.D GESTURE MODELS (TFLite - MediaPipe) =====
        AIModel(
            name = "D.A.V.I.D Gesture Hand",
            url = "https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task",
            size = "25 MB",
            minRamGB = 1,
            type = "Gesture",
            format = "TFLite"
        ),
        AIModel(
            name = "D.A.V.I.D Gesture Recognition",
            url = "https://storage.googleapis.com/mediapipe-models/gesture_recognizer/gesture_recognizer/float16/latest/gesture_recognizer.task",
            size = "31 MB",
            minRamGB = 1,
            type = "Gesture",
            format = "TFLite"
        )
    )
    
    /**
     * Get device RAM in GB
     */
    private fun getDeviceRamGB(): Int {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            val ramGB = (memInfo.totalMem / (1024 * 1024 * 1024)).toInt()
            Log.d(TAG, "Device RAM: $ramGB GB")
            ramGB
        } catch (e: Exception) {
            Log.e(TAG, "Error getting device RAM", e)
            2 // Default to 2GB
        }
    }
    
    /**
     * Get all available models
     */
    fun getAllModels(): List<AIModel> = availableModels
    
    /**
     * Get models by type
     */
    fun getModelsByType(type: String): List<AIModel> {
        return availableModels.filter { it.type == type }
    }
    
    /**
     * Get models suitable for device
     */
    fun getRecommendedModels(): List<AIModel> {
        val deviceRam = getDeviceRamGB()
        return availableModels.filter { it.minRamGB <= deviceRam }
    }
    
    /**
     * Get best LLM for device (for chat)
     */
    fun getRecommendedLLM(): AIModel? {
        val deviceRam = getDeviceRamGB()
        val llmModels = availableModels.filter { it.type == "LLM" }
        val recommended = when {
            deviceRam >= 4 -> llmModels.find { it.name.contains("Pro") }
            deviceRam >= 3 -> llmModels.find { it.name.contains("Standard") }
            else -> llmModels.find { it.name.contains("Light") }
        } ?: llmModels.firstOrNull()
        
        Log.d(TAG, "Recommended LLM for ${deviceRam}GB RAM: ${recommended?.name}")
        return recommended
    }
    
    /**
     * Get best voice model
     */
    fun getRecommendedVoiceModel(): AIModel? {
        val deviceRam = getDeviceRamGB()
        val voiceModels = availableModels.filter { it.type == "Speech" }
        return when {
            deviceRam >= 2 -> voiceModels.find { it.name.contains("Small") }
            else -> voiceModels.find { it.name.contains("Base") } 
                 ?: voiceModels.find { it.name.contains("Tiny") }
        } ?: voiceModels.firstOrNull()
    }
    
    /**
     * Get vision model
     */
    fun getVisionModel(): AIModel? {
        val deviceRam = getDeviceRamGB()
        val visionModels = availableModels.filter { it.type == "Vision" }
        return when {
            deviceRam >= 2 -> visionModels.find { it.name.contains("Standard") }
            else -> visionModels.find { it.name.contains("Lite") }
        } ?: visionModels.firstOrNull()
    }
    
    /**
     * Get gesture recognition models
     */
    fun getGestureModels(): List<AIModel> {
        return availableModels.filter { it.type == "Gesture" }
    }
    
    /**
     * Get language model by name
     */
    fun getLanguageModel(language: String): AIModel? {
        return availableModels.find { 
            it.type == "Language" && it.name.contains(language, ignoreCase = true)
        }
    }
    
    /**
     * Get all language models
     */
    fun getAllLanguageModels(): List<AIModel> {
        return availableModels.filter { it.type == "Language" }
    }
    
    /**
     * Download model from URL
     */
    suspend fun downloadModel(
        model: AIModel, 
        onProgress: (progress: Int) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Starting download: ${model.name}")
            Log.d(TAG, "Format: ${model.format} (Android-compatible)")
            Log.d(TAG, "URL: ${model.url}")
            Log.d(TAG, "Expected size: ${model.size}")
            
            val fileName = model.url.substringAfterLast("/").substringBefore("?")
            val modelFile = File(modelsDir, fileName)
            
            // Skip if already downloaded
            if (modelFile.exists() && modelFile.length() > 1024 * 1024) { // > 1MB
                Log.d(TAG, "Model already exists: ${modelFile.absolutePath}")
                return@withContext Result.success(modelFile.absolutePath)
            }
            
            val request = Request.Builder()
                .url(model.url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                .header("Accept", "*/*")
                .build()
            
            Log.d(TAG, "Sending HTTP request...")
            val response = httpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                val error = "HTTP ${response.code}: ${response.message}"
                Log.e(TAG, "Download failed: $error")
                throw Exception(error)
            }
            
            val body = response.body ?: throw Exception("Empty response body")
            val contentLength = body.contentLength()
            Log.d(TAG, "Content length: $contentLength bytes")
            
            // Download with progress
            FileOutputStream(modelFile).use { output ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(8192)
                    var downloadedBytes = 0L
                    var bytesRead: Int
                    var lastProgress = 0
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        
                        if (contentLength > 0) {
                            val progress = ((downloadedBytes * 100) / contentLength).toInt()
                            if (progress != lastProgress) {
                                onProgress(progress)
                                lastProgress = progress
                                if (progress % 10 == 0) {
                                    Log.d(TAG, "Progress: $progress%")
                                }
                            }
                        }
                    }
                }
            }
            
            Log.d(TAG, "Model saved: ${modelFile.absolutePath}")
            Log.d(TAG, "Format: ${model.format} - Ready for Android")
            Result.success(modelFile.absolutePath)
            
        } catch (e: Exception) {
            Log.e(TAG, "Download failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get downloaded models
     */
    fun getDownloadedModels(): List<File> {
        return try {
            modelsDir.listFiles()?.filter { it.isFile }?.toList() ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting downloaded models", e)
            emptyList()
        }
    }
    
    /**
     * Check if model is downloaded
     */
    fun isModelDownloaded(model: AIModel): Boolean {
        val fileName = model.url.substringAfterLast("/").substringBefore("?")
        val file = File(modelsDir, fileName)
        return file.exists() && file.length() > 1024 * 1024 // > 1MB
    }
    
    /**
     * Get model file path
     */
    fun getModelPath(modelName: String): String? {
        val file = File(modelsDir, modelName)
        return if (file.exists()) file.absolutePath else null
    }
    
    /**
     * Get total models size
     */
    fun getTotalModelsSize(): Long {
        return try {
            getDownloadedModels().sumOf { it.length() }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating total size", e)
            0L
        }
    }
    
    /**
     * Delete model
     */
    fun deleteModel(modelName: String): Boolean {
        return try {
            val file = File(modelsDir, modelName)
            val deleted = file.delete()
            if (deleted) {
                Log.d(TAG, "Deleted model: $modelName")
            }
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting model", e)
            false
        }
    }
    
    /**
     * Delete all models
     */
    fun deleteAllModels(): Boolean {
        return try {
            var allDeleted = true
            getDownloadedModels().forEach { file ->
                if (!file.delete()) {
                    allDeleted = false
                    Log.e(TAG, "Failed to delete: ${file.name}")
                }
            }
            allDeleted
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting all models", e)
            false
        }
    }
    
    companion object {
        private const val TAG = "ModelManager"
    }
}
