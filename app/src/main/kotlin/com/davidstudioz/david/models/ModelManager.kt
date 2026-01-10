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
import java.util.concurrent.TimeUnit

data class AIModel(
    val name: String,
    val url: String,
    val size: String,
    val minRamGB: Int,
    val type: String, // "LLM", "Vision", "Speech", "Language", "Gesture"
    val format: String = "GGUF", // "GGUF", "ONNX", "TFLite", "GGML"
    val language: String = "en" // Language code for language models
)

/**
 * ModelManager - FIXED: Automatic device-based model download
 * ✅ Auto-detects device RAM and downloads appropriate models
 * ✅ Downloads only essential models based on capacity
 * ✅ Proper error handling and retry logic
 * ✅ Progress tracking for all downloads
 */
class ModelManager(private val context: Context) {
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(300, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .build()
        
    private val modelsDir = File(context.filesDir, "david_models")
    
    init {
        try {
            if (!modelsDir.exists()) {
                modelsDir.mkdirs()
                Log.d(TAG, "Created models directory: ${modelsDir.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating models directory", e)
        }
    }
    
    /**
     * Get device RAM in GB
     */
    fun getDeviceRamGB(): Int {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            val ramGB = (memInfo.totalMem / (1024 * 1024 * 1024)).toInt()
            Log.d(TAG, "Device RAM: $ramGB GB")
            ramGB
        } catch (e: Exception) {
            Log.e(TAG, "Error getting device RAM", e)
            2 // Default to 2GB for safety
        }
    }
    
    /**
     * Get essential models to download based on device capacity
     * FIXED: Auto-selects models based on RAM
     */
    fun getEssentialModels(): List<AIModel> {
        val deviceRam = getDeviceRamGB()
        val models = mutableListOf<AIModel>()
        
        // Voice model (always needed)
        models.add(when {
            deviceRam >= 3 -> getVoiceModel("small")!!
            deviceRam >= 2 -> getVoiceModel("base")!!
            else -> getVoiceModel("tiny")!!
        })
        
        // LLM model for chat
        models.add(when {
            deviceRam >= 4 -> getLLMModel("pro")!!
            deviceRam >= 3 -> getLLMModel("standard")!!
            else -> getLLMModel("light")!!
        })
        
        // Vision model
        models.add(if (deviceRam >= 2) getVisionModel("standard")!! else getVisionModel("lite")!!)
        
        // Gesture models (both needed)
        models.addAll(getGestureModels())
        
        // Default language (English)
        models.add(getLanguageModel("English")!!)
        
        Log.d(TAG, "Essential models for ${deviceRam}GB RAM: ${models.size} models")
        return models
    }
    
    private fun getVoiceModel(variant: String): AIModel? {
        return when (variant.lowercase()) {
            "tiny" -> AIModel(
                "D.A.V.I.D Voice Tiny",
                "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.en.bin",
                "75 MB", 1, "Speech", "GGML"
            )
            "base" -> AIModel(
                "D.A.V.I.D Voice Base",
                "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.en.bin",
                "142 MB", 1, "Speech", "GGML"
            )
            "small" -> AIModel(
                "D.A.V.I.D Voice Small",
                "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small.en.bin",
                "466 MB", 2, "Speech", "GGML"
            )
            else -> null
        }
    }
    
    private fun getLLMModel(variant: String): AIModel? {
        return when (variant.lowercase()) {
            "light" -> AIModel(
                "D.A.V.I.D Chat Light",
                "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf",
                "669 MB", 2, "LLM", "GGUF"
            )
            "standard" -> AIModel(
                "D.A.V.I.D Chat Standard",
                "https://huggingface.co/second-state/Qwen1.5-1.8B-Chat-GGUF/resolve/main/qwen1.5-1.8b-chat-q4_k_m.gguf",
                "1.1 GB", 2, "LLM", "GGUF"
            )
            "pro" -> AIModel(
                "D.A.V.I.D Chat Pro",
                "https://huggingface.co/TheBloke/phi-2-GGUF/resolve/main/phi-2.Q4_K_M.gguf",
                "1.6 GB", 3, "LLM", "GGUF"
            )
            else -> null
        }
    }
    
    private fun getVisionModel(variant: String): AIModel? {
        return when (variant.lowercase()) {
            "lite" -> AIModel(
                "D.A.V.I.D Vision Lite",
                "https://github.com/onnx/models/raw/main/validated/vision/classification/mobilenet/model/mobilenetv2-12.onnx",
                "14 MB", 1, "Vision", "ONNX"
            )
            "standard" -> AIModel(
                "D.A.V.I.D Vision Standard",
                "https://github.com/onnx/models/raw/main/validated/vision/classification/resnet/model/resnet50-v2-7.onnx",
                "98 MB", 2, "Vision", "ONNX"
            )
            else -> null
        }
    }
    
    fun getGestureModels(): List<AIModel> {
        return listOf(
            AIModel(
                "D.A.V.I.D Gesture Hand",
                "https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task",
                "25 MB", 1, "Gesture", "TFLite"
            ),
            AIModel(
                "D.A.V.I.D Gesture Recognition",
                "https://storage.googleapis.com/mediapipe-models/gesture_recognizer/gesture_recognizer/float16/latest/gesture_recognizer.task",
                "31 MB", 1, "Gesture", "TFLite"
            )
        )
    }
    
    fun getLanguageModel(language: String): AIModel? {
        val supportedLanguages = mapOf(
            "English" to "en",
            "Hindi" to "hi",
            "Tamil" to "ta",
            "Telugu" to "te",
            "Bengali" to "bn",
            "Marathi" to "mr",
            "Gujarati" to "gu",
            "Kannada" to "kn",
            "Malayalam" to "ml",
            "Punjabi" to "pa"
        )
        
        val langCode = supportedLanguages[language] ?: return null
        
        return AIModel(
            "D.A.V.I.D Language $language",
            "https://tfhub.dev/google/lite-model/universal-sentence-encoder-multilingual/3?lite-format=tflite",
            "50 MB", 1, "Language", "TFLite", langCode
        )
    }
    
    fun getAllLanguages(): List<String> {
        return listOf(
            "English", "Hindi", "Tamil", "Telugu", "Bengali",
            "Marathi", "Gujarati", "Kannada", "Malayalam", "Punjabi"
        )
    }
    
    /**
     * Download model with progress - FIXED
     */
    suspend fun downloadModel(
        model: AIModel,
        onProgress: (progress: Int) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Downloading: ${model.name}")
            
            val fileName = "${model.type.lowercase()}_${model.language}_${System.currentTimeMillis()}.${model.format.lowercase()}"
            val modelFile = File(modelsDir, fileName)
            
            // Check if already exists
            if (modelFile.exists() && modelFile.length() > 1024 * 1024) {
                Log.d(TAG, "Model already exists: ${model.name}")
                return@withContext Result.success(modelFile)
            }
            
            val request = Request.Builder()
                .url(model.url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}: ${response.message}")
            }
            
            val body = response.body ?: throw Exception("Empty response")
            val contentLength = body.contentLength()
            
            FileOutputStream(modelFile).use { output ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(8192)
                    var downloaded = 0L
                    var read: Int
                    
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read
                        
                        if (contentLength > 0) {
                            val progress = ((downloaded * 100) / contentLength).toInt()
                            onProgress(progress)
                        }
                    }
                }
            }
            
            Log.d(TAG, "Downloaded: ${model.name} -> ${modelFile.absolutePath}")
            Result.success(modelFile)
            
        } catch (e: Exception) {
            Log.e(TAG, "Download failed: ${model.name}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if essential models are downloaded
     */
    fun areEssentialModelsDownloaded(): Boolean {
        val essential = getEssentialModels()
        val downloaded = getDownloadedModels()
        return essential.size <= downloaded.size
    }
    
    fun getDownloadedModels(): List<File> {
        return try {
            modelsDir.listFiles()?.filter { it.isFile && it.length() > 1024 * 1024 }?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun getModelPath(type: String, language: String = "en"): File? {
        return getDownloadedModels().firstOrNull { 
            it.name.contains(type.lowercase()) && it.name.contains(language)
        }
    }
    
    fun deleteAllModels(): Boolean {
        return try {
            getDownloadedModels().forEach { it.delete() }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    companion object {
        private const val TAG = "ModelManager"
    }
}
