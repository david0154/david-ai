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
    val type: String // "LLM", "Vision", "Speech"
)

/**
 * ModelManager - Downloads and manages AI models
 * Direct initialization without Hilt
 */
class ModelManager(private val context: Context) {
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
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
     * Available D.A.V.I.D AI Models (GGUF Format from Hugging Face)
     */
    private val availableModels = listOf(
        // LLM Models with D.A.V.I.D AI Branding
        AIModel(
            name = "D.A.V.I.D AI 2B Light",
            url = "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q8_0.gguf",
            size = "1.5 GB",
            minRamGB = 2,
            type = "LLM"
        ),
        AIModel(
            name = "D.A.V.I.D AI 2B Pro",
            url = "https://huggingface.co/TheBloke/phi-2-GGUF/resolve/main/phi-2.Q8_0.gguf",
            size = "1.4 GB",
            minRamGB = 3,
            type = "LLM"
        ),
        AIModel(
            name = "D.A.V.I.D AI 2B",
            url = "https://huggingface.co/second-state/Qwen1.5-1.8B-Chat-GGUF/resolve/main/qwen1.5-1.8b-chat-q8_0.gguf",
            size = "1.3 GB",
            minRamGB = 2,
            type = "LLM"
        ),
        // Vision Models
        AIModel(
            name = "D.A.V.I.D Vision",
            url = "https://huggingface.co/openai/CLIP-vit-base-patch32/resolve/main/model.onnx",
            size = "200 MB",
            minRamGB = 1,
            type = "Vision"
        ),
        // Speech Models
        AIModel(
            name = "D.A.V.I.D Voice",
            url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.en.bin",
            size = "50 MB",
            minRamGB = 1,
            type = "Speech"
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
     * Get models suitable for device
     */
    fun getRecommendedModels(): List<AIModel> {
        val deviceRam = getDeviceRamGB()
        return availableModels.filter { it.minRamGB <= deviceRam }
    }
    
    /**
     * Get best LLM for device
     */
    fun getRecommendedLLM(): AIModel? {
        val deviceRam = getDeviceRamGB()
        val recommended = when {
            deviceRam >= 3 -> availableModels.find { it.name.contains("2B Pro") }
            deviceRam >= 2 -> availableModels.find { it.name.contains("2B") && !it.name.contains("Pro") && !it.name.contains("Light") }
            else -> availableModels.find { it.name.contains("2B Light") }
        }
        Log.d(TAG, "Recommended LLM: ${recommended?.name}")
        return recommended
    }
    
    /**
     * Download model from Hugging Face
     */
    suspend fun downloadModel(model: AIModel, onProgress: (progress: Int) -> Unit = {}): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Starting download: ${model.name} from ${model.url}")
            
            val fileName = model.url.substringAfterLast("/")
            val modelFile = File(modelsDir, fileName)
            
            // Skip if already downloaded
            if (modelFile.exists() && modelFile.length() > 0) {
                Log.d(TAG, "Model already exists: ${modelFile.absolutePath}")
                return@withContext Result.success(modelFile.absolutePath)
            }
            
            val request = Request.Builder()
                .url(model.url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                .build()
            
            Log.d(TAG, "Sending HTTP request...")
            val response = httpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                val error = "Failed to download: HTTP ${response.code}"
                Log.e(TAG, error)
                throw Exception(error)
            }
            
            val body = response.body ?: throw Exception("Empty response body")
            val contentLength = body.contentLength()
            Log.d(TAG, "Content length: $contentLength bytes")
            
            if (contentLength <= 0) {
                throw Exception("Invalid content length")
            }
            
            FileOutputStream(modelFile).use { output ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(8192)
                    var downloadedBytes = 0L
                    var bytesRead: Int
                    var lastProgress = 0
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        
                        val progress = ((downloadedBytes * 100) / contentLength).toInt()
                        if (progress != lastProgress) {
                            onProgress(progress)
                            lastProgress = progress
                            if (progress % 10 == 0) {
                                Log.d(TAG, "Download progress: $progress%")
                            }
                        }
                    }
                }
            }
            
            Log.d(TAG, "Download complete: ${modelFile.absolutePath}")
            Result.success(modelFile.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get downloaded models
     */
    fun getDownloadedModels(): List<File> {
        return try {
            modelsDir.listFiles()?.toList() ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting downloaded models", e)
            emptyList()
        }
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
    
    companion object {
        private const val TAG = "ModelManager"
    }
}
