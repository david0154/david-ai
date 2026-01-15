package com.davidstudioz.david.background

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.TimeUnit

@Singleton
class BackgroundProcessor @Inject constructor(
    private val context: Context
) {
    
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Schedule periodic chat history cleanup
     */
    fun scheduleHistoryCleanup() {
        val cleanupWork = PeriodicWorkRequestBuilder<HistoryCleanupWorker>(
            15,
            TimeUnit.MINUTES
        ).build()
        
        workManager.enqueueUniquePeriodicWork(
            "chat_history_cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupWork
        )
    }
    
    /**
     * Schedule model downloads
     */
    fun scheduleModelDownload(modelUrl: String) {
        val downloadData = Data.Builder()
            .putString("model_url", modelUrl)
            .build()
        
        val downloadWork = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setInputData(downloadData)
            .build()
        
        workManager.enqueueUniqueWork(
            "model_download_${System.currentTimeMillis()}",
            ExistingWorkPolicy.KEEP,
            downloadWork
        )
    }
    
    /**
     * Schedule sync operations
     */
    fun scheduleSyncOperation() {
        val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(
            30,
            TimeUnit.MINUTES
        ).build()
        
        workManager.enqueueUniquePeriodicWork(
            "device_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWork
        )
    }
    
    /**
     * Schedule health tracking
     */
    fun scheduleHealthTracking() {
        val healthWork = PeriodicWorkRequestBuilder<HealthTrackingWorker>(
            1,
            TimeUnit.HOURS
        ).build()
        
        workManager.enqueueUniquePeriodicWork(
            "health_tracking",
            ExistingPeriodicWorkPolicy.KEEP,
            healthWork
        )
    }
    
    /**
     * Cancel all background tasks
     */
    suspend fun cancelAllTasks(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            workManager.cancelAllWork()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

