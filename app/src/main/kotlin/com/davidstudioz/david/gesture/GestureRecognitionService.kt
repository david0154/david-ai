package com.davidstudioz.david.gesture

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.davidstudioz.david.R
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerOptions
import kotlinx.coroutines.*
import java.nio.ByteBuffer

/**
 * Gesture Recognition Service - Detects hand gestures for device control
 * Uses MediaPipe for real-time hand landmark detection and gesture classification
 */
class GestureRecognitionService : LifecycleService() {

    private var gestureRecognizer: GestureRecognizer? = null
    private var camera: Camera? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isDetecting = false
    private var onGestureDetected: ((String, Float) -> Unit)? = null

    companion object {
        private const val TAG = "GestureService"
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "david_gesture_channel"
        
        fun start(context: Context) {
            val intent = Intent(context, GestureRecognitionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stop(context: Context) {
            val intent = Intent(context, GestureRecognitionService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Gesture Recognition Service Created")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        initializeGestureRecognizer()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "D.A.V.I.D Gesture Control",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Hand gesture detection and control"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("D.A.V.I.D Gesture Control")
            .setContentText(if (isDetecting) "Detecting hand gestures..." else "Gesture detection paused")
            .setSmallIcon(R.drawable.ic_gesture)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun initializeGestureRecognizer() {
        try {
            serviceScope.launch(Dispatchers.IO) {
                try {
                    // Check if gesture model file exists
                    val modelFile = java.io.File(filesDir, "david_models/gesture.bin")
                    if (!modelFile.exists()) {
                        Log.w(TAG, "Gesture model not found, using default configuration")
                        withContext(Dispatchers.Main) {
                            isDetecting = false
                            updateNotification()
                        }
                        return@launch
                    }

                    val baseOptions = BaseOptions.builder()
                        .setModelAssetPath(modelFile.absolutePath)
                        .build()

                    val options = GestureRecognizerOptions.builder()
                        .setBaseOptions(baseOptions)
                        .setRunningMode(RunningMode.LIVE_STREAM)
                        .setMinHandDetectionConfidence(0.5f)
                        .setMinHandPresenceConfidence(0.5f)
                        .setMinTrackingConfidence(0.5f)
                        .setResultListener { result, image ->
                            result.gestures().forEach { gestureList ->
                                gestureList.firstOrNull()?.let { gesture ->
                                    val gestureName = gesture.categoryName()
                                    val confidence = gesture.score()
                                    Log.d(TAG, "Gesture detected: $gestureName (${confidence * 100}%)")
                                    onGestureDetected?.invoke(gestureName, confidence)
                                    
                                    // Broadcast gesture detection
                                    val intent = Intent("com.davidstudioz.david.GESTURE_DETECTED").apply {
                                        putExtra("gesture", gestureName)
                                        putExtra("confidence", confidence)
                                    }
                                    sendBroadcast(intent)
                                }
                            }
                        }
                        .setErrorListener { error ->
                            Log.e(TAG, "Gesture recognizer error: ${error.message}")
                        }
                        .build()

                    gestureRecognizer = GestureRecognizer.createFromOptions(this@GestureRecognitionService, options)
                    
                    withContext(Dispatchers.Main) {
                        isDetecting = true
                        updateNotification()
                        Log.d(TAG, "Gesture recognizer initialized successfully")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing gesture recognizer", e)
                    withContext(Dispatchers.Main) {
                        isDetecting = false
                        updateNotification()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in initializeGestureRecognizer", e)
        }
    }

    fun setOnGestureDetectedListener(listener: (String, Float) -> Unit) {
        onGestureDetected = listener
    }

    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "Service started")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                createNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            )
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = super.onBind(intent)

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        serviceScope.cancel()
        isDetecting = false
        camera?.cameraControl?.enableTorch(false)
        gestureRecognizer?.close()
        gestureRecognizer = null
    }
}
