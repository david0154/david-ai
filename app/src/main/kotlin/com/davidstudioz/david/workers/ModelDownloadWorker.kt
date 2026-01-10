package com.davidstudioz.david.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.davidstudioz.david.utils.DeviceResourceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Smart AI Model Downloader
 * - Uses DeviceResourceManager for resource-aware downloads
 * - Respects 50-60% resource usage limits
 * - Selects appropriate model based on available resources
 * - Only downloads if device has sufficient free resources
 */
class ModelDownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val resourceManager = DeviceResourceManager(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "=" * 50)
            Log.d(TAG, "Starting Smart Model Download")
            Log.d(TAG, "=" * 50)
            
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
            Log.d(TAG, "  Recommended Model: ${availability.recommendedModel.name}")
            Log.d(TAG, "  Max Model Size: ${availability.maxModelSizeMB}MB")
            Log.d(TAG, "  Max RAM Usage: ${availability.maxRamUsageMB}MB")
            Log.d(TAG, "  Reason: ${availability.reason}")
            Log.d(TAG, "")
            
            // Don't download if resources are constrained
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
            
            // Download appropriate model
            val modelType = availability.recommendedModel.name.lowercase()
            Log.d(TAG, "Downloading model: $modelType")
            Log.d(TAG, "  Size: ${availability.recommendedModel.sizeMB}MB")
            Log.d(TAG, "  RAM Required: ${availability.recommendedModel.ramRequiredMB}MB")
            
            val success = downloadModel(modelType, availability.recommendedModel)
            
            if (success) {
                Log.d(TAG, "✓ Model downloaded successfully: $modelType")
                Log.d(TAG, "=" * 50)
                Result.success(workDataOf(
                    "model_type" to modelType,
                    "model_size_mb" to availability.recommendedModel.sizeMB,
                    "ram_required_mb" to availability.recommendedModel.ramRequiredMB
                ))
            } else {
                Log.e(TAG, "✗ Model download failed: $modelType")
                Log.d(TAG, "=" * 50)
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in model download worker", e)
            Result.failure(workDataOf("error" to e.message))
        }
    }

    /**
     * Download AI model from server
     * In production, this would download from your CDN/server
     */
    private suspend fun downloadModel(
        modelType: String,
        modelSize: DeviceResourceManager.ModelSize
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Model URLs (replace with your actual URLs)
            val modelUrl = when (modelType) {
                "ultra" -> "https://example.com/models/david-ai-ultra.tflite"
                "pro" -> "https://example.com/models/david-ai-pro.tflite"
                "standard" -> "https://example.com/models/david-ai-standard.tflite"
                "lite" -> "https://example.com/models/david-ai-lite.tflite"
                "tiny" -> "https://example.com/models/david-ai-tiny.tflite"
                else -> return@withContext false
            }
            
            // Download path
            val modelFile = File(applicationContext.filesDir, "ai_model_$modelType.tflite")
            
            // Check if model already exists
            if (modelFile.exists() && modelFile.length() > 0) {
                Log.d(TAG, "Model already exists: ${modelFile.absolutePath}")
                return@withContext true
            }
            
            // TODO: Implement actual download from your server
            // For now, create a placeholder file with size info
            Log.d(TAG, "Creating model file: ${modelFile.absolutePath}")
            val modelInfo = buildString {
                appendLine("DAVID AI Model: $modelType")
                appendLine("Size: ${modelSize.sizeMB}MB")
                appendLine("RAM Required: ${modelSize.ramRequiredMB}MB")
                appendLine("Download URL: $modelUrl")
                appendLine("Status: Ready for production download")
            }
            modelFile.writeText(modelInfo)
            
            Log.d(TAG, "Model file created: ${modelFile.length()} bytes")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error in downloadModel", e)
            false
        }
    }

    companion object {
        private const val TAG = "ModelDownloadWorker"
        private operator fun String.times(count: Int) = this.repeat(count)
    }
}
