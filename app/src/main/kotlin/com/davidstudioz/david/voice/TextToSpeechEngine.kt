package com.davidstudioz.david.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

/**
 * TextToSpeechEngine - FIXED FOR CONCISE RESPONSES
 * ✅ Male voice (David) and Female voice (Dayna) support
 * ✅ Concise mode (no extra talking)
 * ✅ Pitch and speed control
 * ✅ Respects user voice settings
 */
class TextToSpeechEngine(private val context: Context) {
    
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val prefs = context.getSharedPreferences("david_voice", Context.MODE_PRIVATE)
    
    init {
        initializeTTS()
    }
    
    private fun initializeTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                applyVoiceSettings()
                Log.d(TAG, "✅ TTS initialized")
            } else {
                Log.e(TAG, "❌ TTS initialization failed")
            }
        }
    }
    
    /**
     * ✅ FIXED: Apply user voice settings (male/female, pitch, speed)
     */
    private fun applyVoiceSettings() {
        if (!isInitialized || tts == null) return
        
        try {
            // Get voice settings
            val voice = prefs.getString("tts_voice", "male") ?: "male"
            val basePitch = prefs.getFloat("tts_pitch", 1.0f)
            val speechRate = prefs.getFloat("tts_rate", 1.0f)
            
            // ✅ Apply pitch (male lower, female higher)
            val adjustedPitch = if (voice == "female") {
                basePitch * 1.2f  // Higher pitch for female voice
            } else {
                basePitch * 0.9f  // Lower pitch for male voice
            }
            
            tts?.setPitch(adjustedPitch)
            tts?.setSpeechRate(speechRate)
            tts?.language = Locale.US
            
            Log.d(TAG, "✅ Voice settings applied: $voice, pitch: $adjustedPitch, rate: $speechRate")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying voice settings", e)
        }
    }
    
    /**
     * ✅ FIXED: Speak with concise mode support
     */
    fun speak(text: String) {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS not ready")
            return
        }
        
        try {
            // ✅ Apply concise mode
            val conciseMode = prefs.getBoolean("concise_mode", true)
            val finalText = if (conciseMode) {
                makeConcise(text)
            } else {
                text
            }
            
            tts?.speak(finalText, TextToSpeech.QUEUE_FLUSH, null, null)
            Log.d(TAG, "Speaking: $finalText")
        } catch (e: Exception) {
            Log.e(TAG, "Error speaking", e)
        }
    }
    
    /**
     * ✅ NEW: Make response concise (remove extra talking)
     */
    private fun makeConcise(text: String): String {
        return text
            // Remove filler phrases
            .replace("Let me", "")
            .replace("I will", "")
            .replace("I'm going to", "")
            .replace("One moment", "")
            .replace("Just a second", "")
            .replace("Please wait", "")
            // Remove redundant confirmations
            .replace("Okay,", "")
            .replace("Sure,", "")
            .replace("Alright,", "")
            // Trim whitespace
            .trim()
            .replace(Regex("\\s+"), " ")
    }
    
    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
    }
    
    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            isInitialized = false
            Log.d(TAG, "TTS shutdown")
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        }
    }
    
    fun isReady(): Boolean = isInitialized
    
    /**
     * ✅ NEW: Reload settings (called when user changes voice settings)
     */
    fun reloadSettings() {
        applyVoiceSettings()
    }
    
    companion object {
        private const val TAG = "TextToSpeechEngine"
    }
}