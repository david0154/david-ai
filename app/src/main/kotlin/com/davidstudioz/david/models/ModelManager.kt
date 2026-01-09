package com.davidstudioz.david.models

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class AIModel(
    val name: String,
    val url: String,
    val size: String,
    val minRamGB: Int,
    val type: String // "llm", "vision", "stt", "tts"
)

@Singleton
class ModelManager @Inject constructor(
    private val context: Context
) {
    
    private val httpClient = OkHttpClient()
    private val modelsDir = File(context.getExternalFilesDir(null), "models")
    
    init {
        modelsDir.mkdirs()
    }
    
    /**
     * Available Open Source Models (GGUF Format)
     */
    private val availableModels = listOf(
        // LLM Models
        AIModel(
            name = "TinyLLaMA 1.1B",
            url = "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q8_0.gguf",
            size = "1.5 GB",
            minRamGB = 2,
            type = "llm"
        ),
        AIModel(
            name = "Phi-2 7B",
            url = "https://huggingface.co/TheBloke/phi-2-GGUF/resolve/main/phi-2.Q8_0.gguf",
            size = "1.4 GB",
            minRamGB = 3,
            type = "llm"
        ),
        AIModel(
            name = "Qwen 1.8B",
            url = "https://huggingface.co/second-state/Qwen1.5-1.8B-Chat-GGUF/resolve/main/qwen1.5-1.8b-chat-q8_0.gguf",
            size = "1.3 GB",
            minRamGB = 2,
            type = "llm"
        ),
        // Vision Models
        AIModel(
            name = "CLIP ViT-Base",
            url = "https://huggingface.co/openai/CLIP-vit-base-patch32/resolve/main/model.onnx",
            size = "200 MB",
            minRamGB = 1,
            type = "vision"
        ),
        // Speech Models
        AIModel(
            name = "Whisper Tiny (STT)",
            url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.en.bin",
            size = "50 MB",
            minRamGB = 1,
            type = "stt"
        )
    )
    
    /**
     * Get device RAM in GB
     */
    private fun getDeviceRamGB(): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        return (memInfo.totalMem / (1024 * 1024 * 1024)).toInt()
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
        return when {
            deviceRam >= 3 -> availableModels.find { it.name.contains("Phi-2") }
            deviceRam >= 2 -> availableModels.find { it.name.contains("Qwen") }
            else -> availableModels.find { it.name.contains("TinyLLaMA") }
        }
    }
    
    /**
     * Download model from Hugging Face
     */
    suspend fun downloadModel(model: AIModel, onProgress: (progress: Int) -> Unit = {}): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val fileName = model.url.substringAfterLast("/")
            val modelFile = File(modelsDir, fileName)
            
            // Skip if already downloaded
            if (modelFile.exists()) {
                return@withContext Result.success(modelFile.absolutePath)
            }
            
            val request = Request.Builder()
                .url(model.url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                throw Exception("Failed to download: ${response.code}")
            }
            
            val body = response.body ?: throw Exception("Empty response")
            val contentLength = body.contentLength()
            
            FileOutputStream(modelFile).use { output ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(8192)
                    var downloadedBytes = 0L
                    var bytesRead: Int
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        
                        val progress = ((downloadedBytes * 100) / contentLength).toInt()
                        onProgress(progress)
                    }
                }
            }
            
            Result.success(modelFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get downloaded models
     */
    fun getDownloadedModels(): List<File> {
        return modelsDir.listFiles()?.toList() ?: emptyList()
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
        return getDownloadedModels().sumOf { it.length() }
    }
    
    /**
     * Delete model
     */
    fun deleteModel(modelName: String): Boolean {
        val file = File(modelsDir, modelName)
        return file.delete()
    }
}
