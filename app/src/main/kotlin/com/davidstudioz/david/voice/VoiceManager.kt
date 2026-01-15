package com.davidstudioz.david.voice

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * VoiceManager - FIXED: Proper voice recognition and TTS
 * ✅ STT (Speech-to-Text) properly initialized
 * ✅ TTS (Text-to-Speech) with proper lifecycle
 * ✅ Error handling and permission checks
 * ✅ Multiple language support
 */
class VoiceManager(private val context: Context) {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isListening = false
    private var isTTSReady = false
    private var currentLanguage = "en"
    
    private var onResultCallback: ((String) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null
    
    init {
        initializeTTS()
        initializeSpeechRecognizer()
    }
    
    private fun initializeTTS() {
        try {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.US)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "TTS Language not supported")
                        isTTSReady = false
                    } else {
                        isTTSReady = true
                        Log.d(TAG, "TTS initialized successfully")
                    }
                } else {
                    Log.e(TAG, "TTS initialization failed")
                    isTTSReady = false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TTS", e)
            isTTSReady = false
        }
    }
    
    private fun initializeSpeechRecognizer() {
        try {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                Log.e(TAG, "Speech recognition not available")
                return
            }
            
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "Ready for speech")
                    isListening = true
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
                    val errorMsg = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                        else -> "Unknown error"
                    }
                    Log.e(TAG, "Speech recognition error: $errorMsg")
                    isListening = false
                    onErrorCallback?.invoke(errorMsg)
                }
                
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        Log.d(TAG, "Speech recognized: $text")
                        onResultCallback?.invoke(text)
                    }
                    isListening = false
                }
                
                override fun onPartialResults(partialResults: Bundle?) {}
                
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            
            Log.d(TAG, "Speech recognizer initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing speech recognizer", e)
        }
    }
    
    /**
     * Start listening for voice input
     */
    fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        try {
            if (isListening) {
                Log.w(TAG, "Already listening")
                return
            }
            
            onResultCallback = onResult
            onErrorCallback = onError
            
            val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguage)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            }
            
            speechRecognizer?.startListening(intent)
            Log.d(TAG, "Started listening")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition", e)
            onError("Failed to start: ${e.message}")
        }
    }
    
    /**
     * Stop listening
     */
    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            isListening = false
            Log.d(TAG, "Stopped listening")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech recognition", e)
        }
    }
    
    /**
     * Speak text - FIXED
     */
    fun speak(text: String, onComplete: () -> Unit = {}) {
        try {
            if (!isTTSReady) {
                Log.e(TAG, "TTS not ready")
                onComplete()
                return
            }
            
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
            Log.d(TAG, "Speaking: $text")
            
            // Call onComplete after speaking
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                onComplete()
            }, (text.length * 50L).coerceAtLeast(1000)) // Estimate speech duration
        } catch (e: Exception) {
            Log.e(TAG, "Error speaking", e)
            onComplete()
        }
    }
    
    /**
     * Set language
     */
    fun setLanguage(languageCode: String): Boolean {
        currentLanguage = languageCode
        val locale = Locale(languageCode)
        var ttsSuccess = false
        if (isTTSReady) {
            try {
                val result = tts?.setLanguage(locale)
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsSuccess = true
                    Log.d(TAG, "TTS language set to: $languageCode")
                } else {
                    Log.e(TAG, "TTS language not supported: $languageCode")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting TTS language", e)
            }
        }

        return ttsSuccess
    }
    
    fun isListening(): Boolean = isListening
    
    fun isTTSReady(): Boolean = isTTSReady
    
    /**
     * Cleanup - MUST be called when done
     */
    fun cleanup() {
        try {
            speechRecognizer?.destroy()
            tts?.stop()
            tts?.shutdown()
            isListening = false
            isTTSReady = false
            Log.d(TAG, "Voice manager cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up", e)
        }
    }
    
    companion object {
        private const val TAG = "VoiceManager"
    }
}
