package com.davidstudioz.david.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.davidstudioz.david.R
import com.davidstudioz.david.models.AIModel
import com.davidstudioz.david.models.DownloadProgress
import com.davidstudioz.david.models.ModelManager
import com.davidstudioz.david.ui.ModelDownloadActivity
import kotlinx.coroutines.*

/**
 * ModelDownloadService - FOREGROUND SERVICE FOR DOWNLOADS
 * ✅ Prevents download failure when device locks
 * ✅ Uses WakeLock to keep CPU alive during downloads
 * ✅ Shows persistent notification with progress
 * ✅ Battery efficient - releases resources when done
 * ✅ Downloads continue even when screen off
 */
class ModelDownloadService : Service() {

    private lateinit var modelManager: ModelManager
    private lateinit var notificationManager: NotificationManager
    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var currentDownloadJob: Job? = null
    private var currentModel: AIModel? = null
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            modelManager = ModelManager(this)
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Create notification channel
            createNotificationChannel()
            
            // Acquire WakeLock to prevent CPU sleep during downloads
            acquireWakeLock()
            
            Log.d(TAG, "ModelDownloadService created")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            // Start as foreground service with notification
            val notification = createNotification(
                title = "D.A.V.I.D Model Download",
                text = "Preparing download...",
                progress = 0
            )
            startForeground(NOTIFICATION_ID, notification)
            
            // Get model to download from intent
            val modelName = intent?.getStringExtra(EXTRA_MODEL_NAME)
            val modelUrl = intent?.getStringExtra(EXTRA_MODEL_URL)
            val modelType = intent?.getStringExtra(EXTRA_MODEL_TYPE)
            val modelSize = intent?.getStringExtra(EXTRA_MODEL_SIZE)
            val modelLanguage = intent?.getStringExtra(EXTRA_MODEL_LANGUAGE) ?: "en"
            
            if (modelName != null && modelUrl != null && modelType != null && modelSize != null) {
                val model = AIModel(
                    name = modelName,
                    type = modelType,
                    url = modelUrl,
                    size = modelSize,
                    language = modelLanguage,
                    description = "Downloading..."
                )
                
                currentModel = model
                startDownload(model)
            } else {
                Log.e(TAG, "Missing model information in intent")
                stopSelf()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onStartCommand", e)
            stopSelf()
        }
        
        return START_STICKY // Restart if killed by system
    }
    
    private fun startDownload(model: AIModel) {
        currentDownloadJob = serviceScope.launch {
            try {
                Log.d(TAG, "Starting download: ${model.name}")
                
                val result = modelManager.downloadModel(model) { progress ->
                    // Update notification with progress
                    updateNotification(
                        title = "Downloading ${model.name}",
                        text = "${progress.downloadedMB.toInt()}MB / ${progress.totalMB.toInt()}MB",
                        progress = progress.progress
                    )
                }
                
                result.fold(
                    onSuccess = { file ->
                        Log.d(TAG, "✅ Download completed: ${model.name}")
                        showCompletionNotification(model.name, success = true)
                        delay(3000) // Show completion for 3 seconds
                        stopSelf()
                    },
                    onFailure = { error ->
                        Log.e(TAG, "❌ Download failed: ${model.name}", error)
                        showCompletionNotification(model.name, success = false, error = error.message)
                        delay(5000) // Show error for 5 seconds
                        stopSelf()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error during download", e)
                showCompletionNotification(model.name, success = false, error = e.message)
                delay(5000)
                stopSelf()
            }
        }
    }
    
    /**
     * ✅ Acquire WakeLock to prevent CPU sleep during downloads
     */
    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, // Only CPU stays awake, screen can turn off
                "DavidAI::ModelDownloadWakeLock"
            ).apply {
                acquire(60 * 60 * 1000L) // Max 1 hour (safety timeout)
            }
            Log.d(TAG, "✅ WakeLock acquired - downloads will continue during device lock")
        } catch (e: Exception) {
            Log.e(TAG, "Error acquiring WakeLock", e)
        }
    }
    
    /**
     * ✅ Release WakeLock to save battery
     */
    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d(TAG, "✅ WakeLock released - battery saving restored")
                }
            }
            wakeLock = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing WakeLock", e)
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Model Downloads",
                NotificationManager.IMPORTANCE_LOW // Low priority - less intrusive
            ).apply {
                description = "Shows progress of AI model downloads"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(
        title: String,
        text: String,
        progress: Int
    ): android.app.Notification {
        val intent = Intent(this, ModelDownloadActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(true) // Cannot be dismissed while downloading
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .build()
    }
    
    private fun updateNotification(title: String, text: String, progress: Int) {
        try {
            val notification = createNotification(title, text, progress)
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification", e)
        }
    }
    
    private fun showCompletionNotification(
        modelName: String,
        success: Boolean,
        error: String? = null
    ) {
        try {
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(
                    if (success) "✅ Download Complete" else "❌ Download Failed"
                )
                .setContentText(
                    if (success) modelName else "$modelName: ${error ?: "Unknown error"}"
                )
                .setSmallIcon(
                    if (success) android.R.drawable.stat_sys_download_done
                    else android.R.drawable.stat_notify_error
                )
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            
            notificationManager.notify(NOTIFICATION_ID + 1, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing completion notification", e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        try {
            // Cancel ongoing download
            currentDownloadJob?.cancel()
            
            // Release WakeLock to save battery
            releaseWakeLock()
            
            // Cancel coroutine scope
            serviceScope.cancel()
            
            Log.d(TAG, "ModelDownloadService destroyed")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    companion object {
        private const val TAG = "ModelDownloadService"
        private const val CHANNEL_ID = "model_download_channel"
        private const val NOTIFICATION_ID = 1001
        
        const val EXTRA_MODEL_NAME = "model_name"
        const val EXTRA_MODEL_URL = "model_url"
        const val EXTRA_MODEL_TYPE = "model_type"
        const val EXTRA_MODEL_SIZE = "model_size"
        const val EXTRA_MODEL_LANGUAGE = "model_language"
        
        /**
         * ✅ Start download as foreground service (survives device lock)
         */
        fun startDownload(
            context: Context,
            model: AIModel
        ) {
            val intent = Intent(context, ModelDownloadService::class.java).apply {
                putExtra(EXTRA_MODEL_NAME, model.name)
                putExtra(EXTRA_MODEL_URL, model.url)
                putExtra(EXTRA_MODEL_TYPE, model.type)
                putExtra(EXTRA_MODEL_SIZE, model.size)
                putExtra(EXTRA_MODEL_LANGUAGE, model.language)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            
            Log.d(TAG, "📥 Starting foreground download: ${model.name}")
        }
        
        /**
         * Stop download service
         */
        fun stopDownload(context: Context) {
            val intent = Intent(context, ModelDownloadService::class.java)
            context.stopService(intent)
        }
    }
}