package com.davidstudioz.david.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.davidstudioz.david.config.ModelConfig
import com.davidstudioz.david.utils.DeviceResourceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Smart AI Model Downloader
 * - Uses DeviceResourceManager for resource-aware downloads
 * - Respects 50-60% resource usage limits
 * - Downloads real models from Hugging Face (via ModelConfig)
 * - Integrates with existing LLMEngine model selection
 */
class ModelDownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val resourceManager = DeviceResourceManager(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "=".repeat(50))
            Log.d(TAG, "Starting Smart Model Download")
            Log.d(TAG, "=".repeat(50))
            
            // Get current resource status
            val resourceStatus = resourceManager.getResourceStatus()
            
            // Log resource information
            Log.d(TAG, "Device Resources:")
            Log.d(TAG, "  RAM: ${resourceStatus.usedRamMB / 1024}GB / ${resourceStatus.totalRamMB / 1024}GB (${resourceStatus.ramUsagePercent.toInt()}%)")
            Log.d(TAG, "  Storage: ${resourceStatus.usedStorageGB}GB / ${resourceStatus.totalStorageGB}GB (${resourceStatus.storageUsagePercent.toInt()}%)")
            Log.d(TAG, "  CPU: ${resourceStatus.cpuCores} cores (${resourceStatus.cpuUsagePercent.toInt()}% usage)")
            Log.d(TAG, "")
            
            // Check if download is allowed
            val availability = resourceStatus.canUseForAI
            Log.d(TAG, "Resource Availability Check:")
            Log.d(TAG, "  Can Download: ${availability.canDownloadModel}")
            Log.d(TAG, "  Max Model Size: ${availability.maxModelSizeMB}MB")
            Log.d(TAG, "  Max RAM Usage: ${availability.maxRamUsageMB}MB")
            Log.d(TAG, "  Reason: ${availability.reason}")
            Log.d(TAG, "")
            
            // Don't download if resources are constrained (usage > 50%)
            if (!availability.canDownloadModel) {
                Log.w(TAG, "Download blocked: ${availability.reason}")
                Log.w(TAG, "Current RAM usage (${resourceStatus.ramUsagePercent.toInt()}%) or Storage usage (${resourceStatus.storageUsagePercent.toInt()}%) exceeds 50% limit")
                return@withContext Result.failure(
                    workDataOf(
                        "error" to "Insufficient resources",
                        "reason" to availability.reason
                    )
                )
            }
            
            // Select model based on available RAM (matching LLMEngine logic)
            val availableMemGB = resourceStatus.totalRamMB / 1024
            val modelInfo = selectModelBasedOnRAM(availableMemGB)
            
            Log.d(TAG, "Selected Model: ${modelInfo.name}")
            Log.d(TAG, "  URL: ${modelInfo.url}")
            Log.d(TAG, "  Size: ${modelInfo.sizeStr}")
            Log.d(TAG, "  Min RAM: ${modelInfo.minRamGB}GB")
            Log.d(TAG, "")
            
            // Download model
            val success = downloadModel(modelInfo)
            
            if (success) {
                Log.d(TAG, "✓ Model downloaded successfully: ${modelInfo.name}")
                Log.d(TAG, "=".repeat(50))
                Result.success(workDataOf(
                    "model_name" to modelInfo.name,
                    "model_size" to modelInfo.sizeStr,
                    "min_ram_gb" to modelInfo.minRamGB
                ))
            } else {
                Log.e(TAG, "✗ Model download failed: ${modelInfo.name}")
                Log.d(TAG, "=".repeat(50))
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in model download worker", e)
            Result.failure(workDataOf("error" to e.message))
        }
    }

    /**
     * Model information
     */
    data class ModelInfo(
        val name: String,
        val url: String,
        val sizeStr: String,
        val minRamGB: Int
    )

    /**
     * Select model based on available RAM
     * Matches logic from LLMEngine.kt
     */
    private fun selectModelBasedOnRAM(availableMemGB: Long): ModelInfo {
        return when {
            availableMemGB >= 6 -> ModelInfo(
                name = "phi-3-8b",
                url = "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-gguf/resolve/main/Phi-3-mini-4k-instruct-q8_0.gguf",
                sizeStr = "2.4 GB",
                minRamGB = 6
            )
            availableMemGB >= 4 -> ModelInfo(
                name = "phi-3-4b",
                url = "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-gguf/resolve/main/Phi-3-mini-4k-instruct-q4_k_m.gguf",
                sizeStr = "1.2 GB",
                minRamGB = 4
            )
            availableMemGB >= 3 -> ModelInfo(
                name = "qwen-1.8b",
                url = ModelConfig.LLM_QWEN_URL,
                sizeStr = ModelConfig.LLM_QWEN_SIZE,
                minRamGB = ModelConfig.LLM_QWEN_MIN_RAM
            )
            availableMemGB >= 2 -> ModelInfo(
                name = "phi-2",
                url = ModelConfig.LLM_PHI2_URL,
                sizeStr = ModelConfig.LLM_PHI2_SIZE,
                minRamGB = ModelConfig.LLM_PHI2_MIN_RAM
            )
            else -> ModelInfo(
                name = "tinyllama-1b",
                url = ModelConfig.LLM_TINYLLAMA_URL,
                sizeStr = ModelConfig.LLM_TINYLLAMA_SIZE,
                minRamGB = ModelConfig.LLM_TINYLLAMA_MIN_RAM
            )
        }
    }

    /**
     * Download AI model from Hugging Face
     */
    private suspend fun downloadModel(modelInfo: ModelInfo): Boolean = withContext(Dispatchers.IO) {
        try {
            // Model filename from name
            val modelFile = File(applicationContext.filesDir, "${modelInfo.name}.gguf")
            
            // Check if model already exists
            if (modelFile.exists() && modelFile.length() > 0) {
                Log.d(TAG, "Model already exists: ${modelFile.absolutePath}")
                return@withContext true
            }
            
            Log.d(TAG, "Downloading from Hugging Face...")
            Log.d(TAG, "URL: ${modelInfo.url}")
            Log.d(TAG, "Destination: ${modelFile.absolutePath}")
            
            // Download with progress
            val url = URL(modelInfo.url)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            connection.connect()
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val totalSize = connection.contentLength
                Log.d(TAG, "Total size: ${totalSize / (1024 * 1024)}MB")
                
                connection.inputStream.use { input ->
                    FileOutputStream(modelFile).use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalBytesRead = 0L
                        var lastProgress = 0
                        
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            
                            // Log progress every 10%
                            val progress = ((totalBytesRead * 100) / totalSize).toInt()
                            if (progress >= lastProgress + 10) {
                                Log.d(TAG, "Download progress: $progress%")
                                lastProgress = progress
                            }
                        }
                    }
                }
                
                Log.d(TAG, "Model downloaded: ${modelFile.length() / (1024 * 1024)}MB")
                true
            } else {
                Log.e(TAG, "Download failed: HTTP ${connection.responseCode}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading model", e)
            false
        }
    }

    companion object {
        private const val TAG = "ModelDownloadWorker"
    }
}
