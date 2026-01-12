package com.davidstudioz.david.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

/**
 * VoiceController - Handles voice recognition and text-to-speech
 * Connected to: SafeMainActivity, VoiceCommandProcessor, DeviceController
 */
class VoiceController(private val context: Context) {
    
    private var textToSpeech: TextToSpeech? = null
    private var isListening = false
    private var onRecognitionResult: ((String) -> Unit)? = null
    
    init {
        initializeTextToSpeech()
    }
    
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
                Log.d(TAG, "TextToSpeech initialized successfully")
            } else {
                Log.e(TAG, "TextToSpeech initialization failed")
            }
        }
    }
    
    /**
     * Start listening for voice input
     * FIXED: Accepts callback parameter
     * Called by: SafeMainActivity
     */
    fun startListening(onResult: (String) -> Unit) {
        if (isListening) {
            Log.w(TAG, "Already listening")
            return
        }
        
        isListening = true
        onRecognitionResult = onResult
        Log.d(TAG, "Voice recognition started")
        
        // TODO: Implement real speech recognition
        // For now, simulate recognition
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val mockResults = listOf(
                "Turn on WiFi",
                "What's the weather today?",
                "Set brightness to maximum",
                "Tell me a joke",
                "Open settings"
            )
            onResult(mockResults.random())
            isListening = false
        }, 2000)
    }
    
    /**
     * Stop listening for voice input
     * Called by: SafeMainActivity
     */
    fun stopListening() {
        isListening = false
        onRecognitionResult = null
        Log.d(TAG, "Voice recognition stopped")
    }
    
    /**
     * Speak text using TextToSpeech
     * Called by: SafeMainActivity, DeviceController
     */
    fun speak(text: String) {
        if (textToSpeech == null) {
            Log.w(TAG, "TextToSpeech not initialized")
            return
        }
        
        textToSpeech?.speak(text, TextToSpeech.QUEUE_ADD, null, null)
        Log.d(TAG, "Speaking: ${text.take(50)}")
    }
    
    /**
     * Check if currently listening
     */
    fun isListening(): Boolean = isListening
    
    /**
     * Cleanup resources
     * Called by: SafeMainActivity onDestroy
     */
    fun cleanup() {
        stopListening()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        Log.d(TAG, "Voice controller cleaned up")
    }
    
    companion object {
        private const val TAG = "VoiceController"
    }
}
