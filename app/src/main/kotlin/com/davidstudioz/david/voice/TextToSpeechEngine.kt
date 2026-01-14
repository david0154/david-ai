package com.davidstudioz.david.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import java.util.*

/**
 * TextToSpeechEngine - WITH MISSING METHODS
 */
class TextToSpeechEngine(private val context: Context) {
    
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var currentLanguage: Locale = Locale.ENGLISH
    private var currentVoice: Voice? = null
    
    init {
        initializeTTS()
    }
    
    private fun initializeTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                loadLanguageSettings()
                Log.d(TAG, "TTS initialized")
            }
        }
    }
    
    private fun loadLanguageSettings() {
        val prefs = context.getSharedPreferences("voice_settings", Context.MODE_PRIVATE)
        val savedLang = prefs.getString("tts_language", "en") ?: "en"
        setLanguage(savedLang)
    }
    
    fun speak(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }
    
    fun setLanguage(langCode: String) {
        currentLanguage = getLocaleFromCode(langCode)
        tts?.language = currentLanguage
    }
    
    private fun getLocaleFromCode(langCode: String): Locale {
        return when (langCode.lowercase()) {
            "en" -> Locale.ENGLISH
            "hi" -> Locale("hi", "IN")
            "ta" -> Locale("ta", "IN")
            "te" -> Locale("te", "IN")
            "bn" -> Locale("bn", "IN")
            "mr" -> Locale("mr", "IN")
            "gu" -> Locale("gu", "IN")
            "kn" -> Locale("kn", "IN")
            "ml" -> Locale("ml", "IN")
            "pa" -> Locale("pa", "IN")
            "or" -> Locale("or", "IN")
            "ur" -> Locale("ur", "IN")
            else -> Locale.ENGLISH
        }
    }
    
    fun changeVoice(voiceId: String) {
        val voice = getAvailableVoices().find { it.name == voiceId }
        if (voice != null) {
            tts?.voice = voice
            currentVoice = voice
        }
    }
    
    fun getAvailableVoices(): Set<Voice> {
        return tts?.voices ?: emptySet()
    }
    
    fun getCurrentVoice(): Voice? = currentVoice
    
    /**
     * ✅ NEW: Set speech rate
     */
    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate.coerceIn(0.1f, 3.0f))
    }
    
    /**
     * ✅ NEW: Set pitch
     */
    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch.coerceIn(0.1f, 2.0f))
    }
    
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
    
    companion object {
        private const val TAG = "TextToSpeechEngine"
    }
}