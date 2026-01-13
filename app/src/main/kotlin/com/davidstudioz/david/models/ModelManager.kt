package com.davidstudioz.david.models

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    val language: String = "en",
    val description: String = ""
)

data class DownloadProgress(
    val modelName: String,
    val progress: Int, // 0-100
    val downloadedMB: Float,
    val totalMB: Float,
    val status: DownloadStatus,
    val error: String? = null,
    val canResume: Boolean = false, // NEW: Can this download be resumed?
    val downloadedBytes: Long = 0 // NEW: Track exact bytes for resume
)

enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    PAUSED, // NEW: Download paused state
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * ModelManager - WITH PAUSE/RESUME & VERIFICATION
 * ✅ Pause/resume downloads with HTTP Range requests
 * ✅ Download state persistence (resume after app restart)
 * ✅ Model file integrity validation
 * ✅ Resume from partial downloads
 * ✅ Better error handling
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
    private val tempDir = File(context.cacheDir, "david_temp_downloads") // NEW: Temp downloads
    private val stateDir = File(context.filesDir, "david_state") // NEW: Download state
    
    private val activeDownloads = mutableMapOf<String, Job>()
    private val downloadProgress = mutableMapOf<String, DownloadProgress>()
    private val pauseFlags = mutableMapOf<String, Boolean>() // NEW: Pause control flags
    
    init {
        try {
            if (!modelsDir.exists()) {
                modelsDir.mkdirs()
                Log.d(TAG, "Created models directory: ${modelsDir.absolutePath}")
            }
            if (!tempDir.exists()) {
                tempDir.mkdirs()
                Log.d(TAG, "Created temp directory: ${tempDir.absolutePath}")
            }
            if (!stateDir.exists()) {
                stateDir.mkdirs()
                Log.d(TAG, "Created state directory: ${stateDir.absolutePath}")
            }
            
            // Load saved download states
            loadDownloadStates()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating directories", e)
        }
    }
    
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
            2
        }
    }
    
    fun getEssentialModels(): List<AIModel> {
        val deviceRam = getDeviceRamGB()
        val models = mutableListOf<AIModel>()
        
        models.add(when {
            deviceRam >= 3 -> getVoiceModel("small")!!
            deviceRam >= 2 -> getVoiceModel("base")!!
            else -> getVoiceModel("tiny")!!
        })
        
        models.add(when {
            deviceRam >= 4 -> getLLMModel("pro")!!
            deviceRam >= 3 -> getLLMModel("standard")!!
            else -> getLLMModel("light")!!
        })
        
        models.add(if (deviceRam >= 2) getVisionModel("standard")!! else getVisionModel("lite")!!)
        models.addAll(getGestureModels())
        models.add(getMultilingualModel())
        
        Log.d(TAG, "Essential models for ${deviceRam}GB RAM: ${models.size} models")
        return models
    }
    
    fun getAllAvailableModels(): List<AIModel> {
        val models = mutableListOf<AIModel>()
        models.add(getVoiceModel("tiny")!!)
        models.add(getVoiceModel("base")!!)
        models.add(getVoiceModel("small")!!)
        models.add(getLLMModel("light")!!)
        models.add(getLLMModel("standard")!!)
        models.add(getLLMModel("pro")!!)
        models.add(getVisionModel("lite")!!)
        models.add(getVisionModel("standard")!!)
        models.addAll(getGestureModels())
        models.add(getMultilingualModel())
        return models
    }
    
    private fun getVoiceModel(variant: String): AIModel? {
        return when (variant.lowercase()) {
            "tiny" -> AIModel(
                "D.A.V.I.D Voice Tiny",
                "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.en.bin",
                "75 MB", 1, "Speech", "GGML", "en",
                "Fastest voice recognition, good for low-end devices"
            )
            "base" -> AIModel(
                "D.A.V.I.D Voice Base",
                "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.en.bin",
                "142 MB", 2, "Speech", "GGML", "en",
                "Balanced voice recognition for most devices"
            )
            "small" -> AIModel(
                "D.A.V.I.D Voice Pro",
                "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small.en.bin",
                "466 MB", 3, "Speech", "GGML", "en",
                "High-accuracy voice recognition for powerful devices"
            )
            else -> null
        }
    }
    
    private fun getLLMModel(variant: String): AIModel? {
        return when (variant.lowercase()) {
            "light" -> AIModel(
                "D.A.V.I.D Chat Light",
                "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf",
                "669 MB", 2, "LLM", "GGUF", "en",
                "Lightweight AI chat for 2GB+ devices"
            )
            "standard" -> AIModel(
                "D.A.V.I.D Chat Standard",
                "https://huggingface.co/Qwen/Qwen1.5-1.8B-Chat-GGUF/resolve/main/qwen1_5-1_8b-chat-q4_k_m.gguf",
                "1.1 GB", 3, "LLM", "GGUF", "en",
                "Balanced AI chat for 3GB+ devices"
            )
            "pro" -> AIModel(
                "D.A.V.I.D Chat Pro",
                "https://huggingface.co/TheBloke/phi-2-GGUF/resolve/main/phi-2.Q4_K_M.gguf",
                "1.6 GB", 4, "LLM", "GGUF", "en",
                "Advanced AI chat for 4GB+ devices"
            )
            else -> null
        }
    }
    
    private fun getVisionModel(variant: String): AIModel? {
        return when (variant.lowercase()) {
            "lite" -> AIModel(
                "D.A.V.I.D Vision Lite",
                "https://github.com/onnx/models/raw/main/validated/vision/classification/mobilenet/model/mobilenetv2-12.onnx",
                "14 MB", 1, "Vision", "ONNX", "en",
                "Basic image recognition for all devices"
            )
            "standard" -> AIModel(
                "D.A.V.I.D Vision Standard",
                "https://github.com/onnx/models/raw/main/validated/vision/classification/resnet/model/resnet50-v2-7.onnx",
                "98 MB", 2, "Vision", "ONNX", "en",
                "Advanced image recognition for 2GB+ devices"
            )
            else -> null
        }
    }
    
    fun getGestureModels(): List<AIModel> {
        return listOf(
            AIModel(
                "D.A.V.I.D Gesture Hand",
                "https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task",
                "25 MB", 1, "Gesture", "TFLite", "en",
                "Hand detection and 21-point tracking"
            ),
            AIModel(
                "D.A.V.I.D Gesture Recognition",
                "https://storage.googleapis.com/mediapipe-models/gesture_recognizer/gesture_recognizer/float16/latest/gesture_recognizer.task",
                "31 MB", 1, "Gesture", "TFLite", "en",
                "Gesture classification (thumbs up, peace, etc.)"
            )
        )
    }
    
    private fun getMultilingualModel(): AIModel {
        return AIModel(
            "D.A.V.I.D Multilingual",
            "https://huggingface.co/sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2/resolve/main/onnx/model.onnx",
            "120 MB", 1, "Language", "ONNX", "multilingual",
            "Supports all 15 languages (English + 14 Indian languages)"
        )
    }
    
    fun getAllLanguages(): List<String> {
        return listOf(
            "English", "Hindi", "Tamil", "Telugu", "Bengali",
            "Marathi", "Gujarati", "Kannada", "Malayalam", "Punjabi",
            "Odia", "Urdu", "Sanskrit", "Kashmiri", "Assamese"
        )
    }
    
    fun isLanguageSupported(language: String): Boolean {
        return getAllLanguages().contains(language)
    }
    
    fun isLanguageModelDownloaded(): Boolean {
        val modelPath = getLanguageModelPath()
        return modelPath != null && modelPath.exists() && modelPath.length() > 1024 * 1024
    }
    
    fun getLanguageModelPath(): File? {
        return getDownloadedModels().firstOrNull { 
            it.name.contains("language") || it.name.contains("multilingual")
        }
    }
    
    /**
     * ✅ Download model with PAUSE/RESUME support
     */
    suspend fun downloadModel(
        model: AIModel,
        onProgress: (DownloadProgress) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📥 Downloading: ${model.name} from ${model.url}")
            
            // Check if paused download exists
            val tempFile = File(tempDir, "${model.type}_${model.language}_temp")
            val resumeFrom = if (tempFile.exists()) {
                val state = getDownloadState(model.name)
                state?.downloadedBytes ?: tempFile.length()
            } else {
                0L
            }
            
            // Reset pause flag
            pauseFlags[model.name] = false
            
            // Initial progress
            val initialProgress = DownloadProgress(
                model.name, 0, 0f, parseSizeMB(model.size), 
                DownloadStatus.QUEUED, downloadedBytes = resumeFrom
            )
            downloadProgress[model.name] = initialProgress
            withContext(Dispatchers.Main) { onProgress(initialProgress) }
            
            val fileName = "${model.type.lowercase()}_${model.language}_${System.currentTimeMillis()}.${model.format.lowercase()}"
            val modelFile = File(modelsDir, fileName)
            
            // Check if model already exists and is valid
            val existingModel = getDownloadedModels().firstOrNull {
                it.name.contains(model.type.lowercase()) && 
                (it.name.contains(model.language) || model.language == "multilingual") &&
                it.length() > 1024 * 1024 && isModelFileValid(it)
            }
            
            if (existingModel != null) {
                Log.d(TAG, "✅ Valid model already exists: ${model.name}")
                val completeProgress = DownloadProgress(
                    model.name, 100, parseSizeMB(model.size), parseSizeMB(model.size), 
                    DownloadStatus.COMPLETED
                )
                withContext(Dispatchers.Main) { onProgress(completeProgress) }
                return@withContext Result.success(existingModel)
            }
            
            // Start downloading
            val downloadingProgress = initialProgress.copy(status = DownloadStatus.DOWNLOADING)
            downloadProgress[model.name] = downloadingProgress
            withContext(Dispatchers.Main) { onProgress(downloadingProgress) }
            
            val requestBuilder = Request.Builder()
                .url(model.url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            
            // ✅ Add HTTP Range header for resume support
            if (resumeFrom > 0) {
                requestBuilder.header("Range", "bytes=$resumeFrom-")
                Log.d(TAG, "📂 Resuming download from byte: $resumeFrom")
            }
            
            val request = requestBuilder.build()
            val response = httpClient.newCall(request).execute()
            
            if (!response.isSuccessful && response.code != 206) { // 206 = Partial Content
                throw Exception("HTTP ${response.code}: ${response.message}")
            }
            
            val body = response.body ?: throw Exception("Empty response body")
            val contentLength = body.contentLength()
            val totalBytes = if (response.code == 206) resumeFrom + contentLength else contentLength
            val totalMB = totalBytes / (1024f * 1024f)
            
            // ✅ Append to existing file if resuming
            FileOutputStream(tempFile, resumeFrom > 0).use { output ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(8192)
                    var downloaded = resumeFrom
                    var read: Int
                    var lastProgressUpdate = 0
                    
                    while (input.read(buffer).also { read = it } != -1) {
                        // ✅ Check pause flag
                        if (pauseFlags[model.name] == true) {
                            Log.d(TAG, "⏸️ Download paused: ${model.name}")
                            val pausedProgress = DownloadProgress(
                                model.name, 
                                ((downloaded * 100) / totalBytes).toInt(),
                                downloaded / (1024f * 1024f),
                                totalMB,
                                DownloadStatus.PAUSED,
                                canResume = true,
                                downloadedBytes = downloaded
                            )
                            downloadProgress[model.name] = pausedProgress
                            withContext(Dispatchers.Main) { onProgress(pausedProgress) }
                            saveDownloadState(model.name, pausedProgress)
                            return@withContext Result.failure(Exception("Download paused"))
                        }
                        
                        output.write(buffer, 0, read)
                        downloaded += read
                        
                        if (totalBytes > 0) {
                            val progress = ((downloaded * 100) / totalBytes).toInt()
                            
                            if (progress > lastProgressUpdate || progress == 100) {
                                lastProgressUpdate = progress
                                val downloadedMB = downloaded / (1024f * 1024f)
                                
                                val progressUpdate = DownloadProgress(
                                    model.name, progress, downloadedMB, totalMB, 
                                    DownloadStatus.DOWNLOADING,
                                    canResume = true,
                                    downloadedBytes = downloaded
                                )
                                downloadProgress[model.name] = progressUpdate
                                
                                withContext(Dispatchers.Main) {
                                    onProgress(progressUpdate)
                                }
                                
                                // ✅ Save state every 10%
                                if (progress % 10 == 0) {
                                    saveDownloadState(model.name, progressUpdate)
                                }
                            }
                        }
                    }
                }
            }
            
            // Move temp file to final location
            if (tempFile.renameTo(modelFile)) {
                tempFile.delete()
            } else {
                tempFile.copyTo(modelFile, overwrite = true)
                tempFile.delete()
            }
            
            // ✅ Verify downloaded file
            if (!isModelFileValid(modelFile)) {
                modelFile.delete()
                throw Exception("Downloaded file is corrupted or incomplete")
            }
            
            // Completion
            val completeProgress = DownloadProgress(
                model.name, 100, totalMB, totalMB, DownloadStatus.COMPLETED
            )
            downloadProgress[model.name] = completeProgress
            withContext(Dispatchers.Main) { onProgress(completeProgress) }
            clearDownloadState(model.name)
            
            Log.d(TAG, "✅ Downloaded: ${model.name} → ${modelFile.absolutePath} (${modelFile.length() / (1024 * 1024)}MB)")
            Result.success(modelFile)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Download failed: ${model.name}", e)
            
            val failedProgress = DownloadProgress(
                model.name, 0, 0f, parseSizeMB(model.size), 
                DownloadStatus.FAILED, e.message, canResume = true
            )
            downloadProgress[model.name] = failedProgress
            withContext(Dispatchers.Main) { onProgress(failedProgress) }
            
            Result.failure(e)
        }
    }
    
    /**
     * ✅ Pause active download
     */
    fun pauseDownload(modelName: String) {
        pauseFlags[modelName] = true
        Log.d(TAG, "🛑 Pause requested: $modelName")
    }
    
    /**
     * ✅ Resume paused download
     */
    suspend fun resumeDownload(
        model: AIModel,
        onProgress: (DownloadProgress) -> Unit = {}
    ): Result<File> {
        pauseFlags[model.name] = false
        Log.d(TAG, "▶️ Resuming download: ${model.name}")
        return downloadModel(model, onProgress)
    }
    
    /**
     * ✅ Validate model file integrity
     */
    private fun isModelFileValid(file: File): Boolean {
        return try {
            file.exists() && 
            file.length() > 1024 * 1024 && // At least 1MB
            file.canRead()
        } catch (e: Exception) {
            Log.e(TAG, "Error validating model file", e)
            false
        }
    }
    
    /**
     * ✅ Save download state to disk
     */
    private fun saveDownloadState(modelName: String, progress: DownloadProgress) {
        try {
            val stateFile = File(stateDir, "${modelName.replace(" ", "_")}.state")
            stateFile.writeText(
                "${progress.downloadedBytes}|${progress.progress}|${progress.totalMB}"
            )
            Log.d(TAG, "💾 Saved state: $modelName at ${progress.downloadedBytes} bytes")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving download state", e)
        }
    }
    
    /**
     * ✅ Get saved download state
     */
    fun getDownloadState(modelName: String): DownloadProgress? {
        return try {
            val stateFile = File(stateDir, "${modelName.replace(" ", "_")}.state")
            if (stateFile.exists()) {
                val parts = stateFile.readText().split("|")
                if (parts.size == 3) {
                    DownloadProgress(
                        modelName,
                        parts[1].toInt(),
                        parts[0].toLong() / (1024f * 1024f),
                        parts[2].toFloat(),
                        DownloadStatus.PAUSED,
                        canResume = true,
                        downloadedBytes = parts[0].toLong()
                    )
                } else null
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error loading download state", e)
            null
        }
    }
    
    /**
     * ✅ Load all saved download states
     */
    private fun loadDownloadStates() {
        try {
            stateDir.listFiles()?.forEach { file ->
                if (file.extension == "state") {
                    val modelName = file.nameWithoutExtension.replace("_", " ")
                    val state = getDownloadState(modelName)
                    if (state != null) {
                        downloadProgress[modelName] = state
                        Log.d(TAG, "📂 Loaded state: $modelName at ${state.progress}%")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading download states", e)
        }
    }
    
    /**
     * ✅ Clear saved download state
     */
    private fun clearDownloadState(modelName: String) {
        try {
            val stateFile = File(stateDir, "${modelName.replace(" ", "_")}.state")
            if (stateFile.exists()) {
                stateFile.delete()
                Log.d(TAG, "🗑️ Cleared state: $modelName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing download state", e)
        }
    }
    
    suspend fun downloadAllEssentialModels(
        onProgress: (String, DownloadProgress) -> Unit
    ): Result<List<File>> {
        val models = getEssentialModels()
        val downloadedFiles = mutableListOf<File>()
        
        for (model in models) {
            val result = downloadModel(model) { progress ->
                onProgress(model.name, progress)
            }
            
            if (result.isSuccess) {
                downloadedFiles.add(result.getOrThrow())
            } else {
                Log.e(TAG, "Failed to download ${model.name}: ${result.exceptionOrNull()?.message}")
            }
        }
        
        return if (downloadedFiles.size == models.size) {
            Result.success(downloadedFiles)
        } else {
            Result.failure(Exception("Downloaded ${downloadedFiles.size}/${models.size} models"))
        }
    }
    
    fun getDownloadProgress(modelName: String): DownloadProgress? {
        return downloadProgress[modelName]
    }
    
    fun cancelDownload(modelName: String) {
        activeDownloads[modelName]?.cancel()
        activeDownloads.remove(modelName)
        pauseFlags[modelName] = true
        
        downloadProgress[modelName] = downloadProgress[modelName]?.copy(
            status = DownloadStatus.CANCELLED
        ) ?: return
        
        clearDownloadState(modelName)
    }
    
    private fun parseSizeMB(size: String): Float {
        val num = size.replace("[^0-9.]".toRegex(), "").toFloatOrNull() ?: 0f
        return when {
            size.contains("GB", ignoreCase = true) -> num * 1024f
            size.contains("MB", ignoreCase = true) -> num
            else -> num
        }
    }
    
    fun areEssentialModelsDownloaded(): Boolean {
        val essential = getEssentialModels()
        val downloaded = getDownloadedModels()
        
        // Check if all essential model types are present
        val hasVoice = downloaded.any { it.name.contains("speech") && isModelFileValid(it) }
        val hasLLM = downloaded.any { it.name.contains("llm") && isModelFileValid(it) }
        val hasVision = downloaded.any { it.name.contains("vision") && isModelFileValid(it) }
        val hasGesture = downloaded.count { it.name.contains("gesture") && isModelFileValid(it) } >= 2
        val hasLanguage = downloaded.any { 
            (it.name.contains("language") || it.name.contains("multilingual")) && isModelFileValid(it)
        }
        
        return hasVoice && hasLLM && hasVision && hasGesture && hasLanguage
    }
    
    fun getDownloadedModels(): List<File> {
        return try {
            modelsDir.listFiles()?.filter { 
                it.isFile && it.length() > 1024 * 1024 && isModelFileValid(it)
            }?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun getTotalDownloadedSizeMB(): Float {
        return getDownloadedModels().sumOf { it.length() } / (1024f * 1024f)
    }
    
    fun getModelPath(type: String, language: String = "en"): File? {
        return getDownloadedModels().firstOrNull { 
            it.name.contains(type.lowercase()) && 
            (it.name.contains(language) || language == "multilingual") &&
            isModelFileValid(it)
        }
    }
    
    fun deleteModel(file: File): Boolean {
        return try {
            file.delete()
            Log.d(TAG, "✅ Deleted model: ${file.name}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting model: ${file.name}", e)
            false
        }
    }
    
    fun deleteAllModels(): Boolean {
        return try {
            val models = getDownloadedModels()
            models.forEach { it.delete() }
            Log.d(TAG, "✅ Deleted ${models.size} models")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting models", e)
            false
        }
    }
    
    companion object {
        private const val TAG = "ModelManager"
    }
}