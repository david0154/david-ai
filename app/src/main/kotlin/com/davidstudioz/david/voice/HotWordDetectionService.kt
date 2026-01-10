package com.davidstudioz.david.voice

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat
import com.davidstudioz.david.R
import kotlinx.coroutines.*

/**
 * Hot Word Detection Service - Listens for activation phrases like "Hey David"
 * Runs in foreground to continuously listen for voice commands
 */
class HotWordDetectionService : Service() {

    private var speechRecognizer: SpeechRecognizer? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isListening = false
    private var hotWords = listOf("hey david", "hi david", "hello david", "david")
    private var onHotWordDetected: ((String) -> Unit)? = null

    companion object {
        private const val TAG = "HotWordService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "david_voice_channel"
        
        fun start(context: Context) {
            val intent = Intent(context, HotWordDetectionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stop(context: Context) {
            val intent = Intent(context, HotWordDetectionService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Hot Word Detection Service Created")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        initializeSpeechRecognizer()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "D.A.V.I.D Voice Assistant",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Voice detection and commands"
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
            .setContentTitle("D.A.V.I.D Voice Assistant")
            .setContentText(if (isListening) "Listening for 'Hey David'..." else "Voice detection paused")
            .setSmallIcon(R.drawable.ic_mic)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun initializeSpeechRecognizer() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(this)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: android.os.Bundle?) {
                            Log.d(TAG, "Ready for speech")
                            isListening = true
                            updateNotification()
                        }

                        override fun onBeginningOfSpeech() {
                            Log.d(TAG, "Speech started")
                        }

                        override fun onRmsChanged(rmsdB: Float) {}

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            Log.d(TAG, "Speech ended")
                            isListening = false
                        }

                        override fun onError(error: Int) {
                            Log.e(TAG, "Speech recognition error: $error")
                            isListening = false
                            // Restart listening after error
                            serviceScope.launch {
                                delay(1000)
                                startListening()
                            }
                        }

                        override fun onResults(results: android.os.Bundle?) {
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            matches?.forEach { text ->
                                Log.d(TAG, "Recognized: $text")
                                checkForHotWord(text.lowercase())
                            }
                            // Continue listening
                            serviceScope.launch {
                                delay(500)
                                startListening()
                            }
                        }

                        override fun onPartialResults(partialResults: android.os.Bundle?) {}

                        override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
                    })
                }
                startListening()
            } else {
                Log.e(TAG, "Speech recognition not available")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing speech recognizer", e)
        }
    }

    private fun startListening() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer?.startListening(intent)
            Log.d(TAG, "Started listening for hot words")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting listening", e)
        }
    }

    private fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            isListening = false
            updateNotification()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping listening", e)
        }
    }

    private fun checkForHotWord(text: String) {
        for (hotWord in hotWords) {
            if (text.contains(hotWord)) {
                Log.d(TAG, "Hot word detected: $hotWord")
                onHotWordDetected?.invoke(text)
                // Broadcast hot word detection
                val intent = Intent("com.davidstudioz.david.HOTWORD_DETECTED").apply {
                    putExtra("text", text)
                    putExtra("hotword", hotWord)
                }
                sendBroadcast(intent)
                break
            }
        }
    }

    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun setOnHotWordDetectedListener(listener: (String) -> Unit) {
        onHotWordDetected = listener
    }

    fun addHotWord(word: String) {
        if (word.isNotBlank() && !hotWords.contains(word.lowercase())) {
            hotWords = hotWords + word.lowercase()
            Log.d(TAG, "Added hot word: $word")
        }
    }

    fun removeHotWord(word: String) {
        hotWords = hotWords.filter { it != word.lowercase() }
        Log.d(TAG, "Removed hot word: $word")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                createNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        serviceScope.cancel()
        stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
