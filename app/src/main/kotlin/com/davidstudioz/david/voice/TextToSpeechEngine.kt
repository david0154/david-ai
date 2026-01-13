package com.davidstudioz.david.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

/**
 * TextToSpeechEngine - Enhanced with speed and pitch control
 * ✅ Supports multiple voices
 * ✅ Speed and pitch adjustment
 * ✅ Voice selection (David/Dayana)
 */
class TextToSpeechEngine(private val context: Context) {
    
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val prefs = context.getSharedPreferences("voice_settings", Context.MODE_PRIVATE)
    
    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                
                // Set default language
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported")
                } else {
                    Log.d(TAG, "✅ TTS initialized successfully")
                }
                
                // Load saved voice parameters
                val speed = prefs.getFloat("voice_speed", 1.0f)
                val pitch = prefs.getFloat("voice_pitch", 1.0f)
                setSpeechRate(speed)
                setPitch(pitch)
                
                // Set voice based on selection
                setVoiceFromPreferences()
            } else {
                Log.e(TAG, "TTS initialization failed")
                isInitialized = false
            }
        }
    }
    
    /**
     * Speak text (no SupportedLanguage parameter)
     */
    fun speak(text: String) {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS not initialized")
            return
        }
        
        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            Log.d(TAG, "Speaking: $text")
        } catch (e: Exception) {
            Log.e(TAG, "Error speaking", e)
        }
    }
    
    /**
     * Set speech rate (0.5 - 2.0)
     */
    fun setSpeechRate(rate: Float) {
        try {
            // Clamp rate between 0.5 and 2.0
            val clampedRate = rate.coerceIn(0.5f, 2.0f)
            tts?.setSpeechRate(clampedRate)
            Log.d(TAG, "Speech rate set to: $clampedRate")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting speech rate", e)
        }
    }
    
    /**
     * Set pitch (0.5 - 2.0)
     */
    fun setPitch(pitch: Float) {
        try {
            // Clamp pitch between 0.5 and 2.0
            val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
            tts?.setPitch(clampedPitch)
            Log.d(TAG, "Pitch set to: $clampedPitch")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting pitch", e)
        }
    }
    
    /**
     * Set voice based on user preference
     */
    private fun setVoiceFromPreferences() {
        try {
            val selectedVoice = prefs.getString("selected_voice", "david") ?: "david"
            
            // Get available voices
            val voices = tts?.voices ?: return
            
            // Try to find matching voice
            val voice = when (selectedVoice) {
                "david" -> {
                    // Try to find male voice
                    voices.firstOrNull { 
                        it.name.contains("male", ignoreCase = true) && 
                        !it.name.contains("female", ignoreCase = true)
                    } ?: voices.firstOrNull()
                }
                "dayana" -> {
                    // Try to find female voice
                    voices.firstOrNull { 
                        it.name.contains("female", ignoreCase = true)
                    } ?: voices.firstOrNull()
                }
                else -> voices.firstOrNull()
            }
            
            voice?.let {
                tts?.voice = it
                Log.d(TAG, "Voice set to: ${it.name}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting voice", e)
        }
    }
    
    /**
     * Change voice (call after user changes selection)
     */
    fun changeVoice(voiceId: String) {
        prefs.edit().putString("selected_voice", voiceId).apply()
        setVoiceFromPreferences()
    }
    
    /**
     * Stop speaking
     */
    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
    }
    
    /**
     * Cleanup (called automatically)
     */
    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
            Log.d(TAG, "TTS engine shutdown")
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        }
    }
    
    companion object {
        private const val TAG = "TextToSpeechEngine"
    }
}