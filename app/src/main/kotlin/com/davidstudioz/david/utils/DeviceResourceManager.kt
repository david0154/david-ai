package com.davidstudioz.david.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.Log
import java.io.File

/**
 * Smart Resource Manager
 * - Monitors RAM, Storage, CPU usage
 * - Ensures AI only uses 50-60% of available resources
 * - Selects best model that fits in resource limits
 */
class DeviceResourceManager(private val context: Context) {

    data class ResourceStatus(
        val totalRamMB: Long,
        val availableRamMB: Long,
        val usedRamMB: Long,
        val ramUsagePercent: Float,
        val totalStorageGB: Long,
        val availableStorageGB: Long,
        val usedStorageGB: Long,
        val storageUsagePercent: Float,
        val cpuCores: Int,
        val cpuUsagePercent: Float,
        val canUseForAI: ResourceAvailability
    )

    data class ResourceAvailability(
        val canDownloadModel: Boolean,
        val maxModelSizeMB: Long,
        val maxRamUsageMB: Long,
        val recommendedModel: ModelSize,
        val reason: String
    )

    enum class ModelSize(val sizeMB: Long, val ramRequiredMB: Long) {
        TINY(50, 256),      // Ultra-light model
        LITE(150, 512),     // Light model
        STANDARD(400, 1024), // Standard model
        PRO(1000, 2048),    // Pro model
        ULTRA(2500, 4096)   // Ultra model (high-end only)
    }

    /**
     * Get complete resource status
     */
    fun getResourceStatus(): ResourceStatus {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        // RAM calculation
        val totalRamMB = memoryInfo.totalMem / (1024 * 1024)
        val availableRamMB = memoryInfo.availMem / (1024 * 1024)
        val usedRamMB = totalRamMB - availableRamMB
        val ramUsagePercent = (usedRamMB.toFloat() / totalRamMB.toFloat()) * 100f

        // Storage calculation
        val stat = StatFs(Environment.getDataDirectory().path)
        val totalStorageBytes = stat.blockCountLong * stat.blockSizeLong
        val availableStorageBytes = stat.availableBlocksLong * stat.blockSizeLong
        val totalStorageGB = totalStorageBytes / (1024 * 1024 * 1024)
        val availableStorageGB = availableStorageBytes / (1024 * 1024 * 1024)
        val usedStorageGB = totalStorageGB - availableStorageGB
        val storageUsagePercent = (usedStorageGB.toFloat() / totalStorageGB.toFloat()) * 100f

        // CPU info
        val cpuCores = Runtime.getRuntime().availableProcessors()
        val cpuUsagePercent = getCpuUsage()

        // Check availability for AI (50-60% limit)
        val availability = checkResourceAvailability(
            totalRamMB, availableRamMB, ramUsagePercent,
            totalStorageGB, availableStorageGB, storageUsagePercent,
            cpuCores
        )

        return ResourceStatus(
            totalRamMB = totalRamMB,
            availableRamMB = availableRamMB,
            usedRamMB = usedRamMB,
            ramUsagePercent = ramUsagePercent,
            totalStorageGB = totalStorageGB,
            availableStorageGB = availableStorageGB,
            usedStorageGB = usedStorageGB,
            storageUsagePercent = storageUsagePercent,
            cpuCores = cpuCores,
            cpuUsagePercent = cpuUsagePercent,
            canUseForAI = availability
        )
    }

    /**
     * Check if resources available for AI (50-60% limit)
     */
    private fun checkResourceAvailability(
        totalRamMB: Long,
        availableRamMB: Long,
        ramUsagePercent: Float,
        totalStorageGB: Long,
        availableStorageGB: Long,
        storageUsagePercent: Float,
        cpuCores: Int
    ): ResourceAvailability {
        
        // Calculate max AI can use (60% of total)
        val maxAIRamMB = (totalRamMB * 0.6f).toLong()
        val maxAIStorageGB = (totalStorageGB * 0.6f).toLong()

        // Available for AI = max allowed - currently used
        val availableForAIRamMB = maxAIRamMB - (totalRamMB - availableRamMB)
        val availableForAIStorageGB = maxAIStorageGB - (totalStorageGB - availableStorageGB)

        // Convert storage to MB for model size
        val availableForAIStorageMB = availableForAIStorageGB * 1024

        // Safety check: Don't download if usage > 50%
        if (ramUsagePercent > 50f || storageUsagePercent > 50f) {
            return ResourceAvailability(
                canDownloadModel = false,
                maxModelSizeMB = 0,
                maxRamUsageMB = 0,
                recommendedModel = ModelSize.TINY,
                reason = "Current usage too high (RAM: ${ramUsagePercent.toInt()}%, Storage: ${storageUsagePercent.toInt()}%)"
            )
        }

        // Determine best model that fits
        val recommendedModel = when {
            availableForAIRamMB >= 4096 && availableForAIStorageMB >= 2500 && cpuCores >= 8 -> 
                ModelSize.ULTRA
            availableForAIRamMB >= 2048 && availableForAIStorageMB >= 1000 && cpuCores >= 6 -> 
                ModelSize.PRO
            availableForAIRamMB >= 1024 && availableForAIStorageMB >= 400 && cpuCores >= 4 -> 
                ModelSize.STANDARD
            availableForAIRamMB >= 512 && availableForAIStorageMB >= 150 -> 
                ModelSize.LITE
            else -> 
                ModelSize.TINY
        }

        val canDownload = availableForAIStorageMB >= recommendedModel.sizeMB &&
                         availableForAIRamMB >= recommendedModel.ramRequiredMB

        return ResourceAvailability(
            canDownloadModel = canDownload,
            maxModelSizeMB = availableForAIStorageMB.coerceAtMost(maxAIStorageGB * 1024),
            maxRamUsageMB = availableForAIRamMB,
            recommendedModel = recommendedModel,
            reason = if (canDownload) 
                "Resources available for ${recommendedModel.name} model" 
            else 
                "Insufficient resources (Need ${recommendedModel.sizeMB}MB storage, ${recommendedModel.ramRequiredMB}MB RAM)"
        )
    }

    /**
     * Get CPU usage percentage (approximate)
     */
    private fun getCpuUsage(): Float {
        return try {
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            val maxMemory = runtime.maxMemory()
            ((usedMemory.toFloat() / maxMemory.toFloat()) * 100f).coerceIn(0f, 100f)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting CPU usage", e)
            0f
        }
    }

    companion object {
        private const val TAG = "DeviceResourceManager"
    }
}
