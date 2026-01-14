package com.davidstudioz.david.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import java.util.*

/**
 * TextToSpeechEngine - WITH MALE/FEMALE VOICE SUPPORT
 * ✅ NEW: Male/Female voice selection
 * ✅ NEW: Speech rate and pitch controls
 * ✅ Multi-language support (15 Indian languages)
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
                Log.d(TAG, "✅ TTS initialized successfully")
            } else {
                Log.e(TAG, "❌ TTS initialization failed")
            }
        }
    }
    
    private fun loadLanguageSettings() {
        val prefs = context.getSharedPreferences("voice_settings", Context.MODE_PRIVATE)
        val savedLang = prefs.getString("tts_language", "en") ?: "en"
        val savedGender = prefs.getString("tts_gender", "male") ?: "male"
        setLanguage(savedLang)
        selectVoiceByGender(savedGender)
    }
    
    fun speak(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.w(TAG, "⚠️ TTS not initialized, cannot speak")
        }
    }
    
    fun setLanguage(langCode: String) {
        currentLanguage = getLocaleFromCode(langCode)
        tts?.language = currentLanguage
        Log.d(TAG, "Language set to: ${currentLanguage.displayLanguage}")
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
            "as" -> Locale("as", "IN")
            "ks" -> Locale("ks", "IN")
            "sa" -> Locale("sa", "IN")
            else -> Locale.ENGLISH
        }
    }
    
    /**
     * ✅ NEW: Select voice by gender preference
     */
    fun selectVoiceByGender(gender: String) {
        if (tts == null || !isInitialized) return
        
        try {
            val voices = tts?.voices?.filter { voice ->
                voice.locale.language == currentLanguage.language &&
                !voice.isNetworkConnectionRequired
            } ?: emptySet()
            
            val targetVoice = when (gender.lowercase()) {
                "male" -> voices.firstOrNull { 
                    it.name.contains("male", ignoreCase = true) && 
                    !it.name.contains("female", ignoreCase = true)
                }
                "female" -> voices.firstOrNull { 
                    it.name.contains("female", ignoreCase = true) 
                }
                else -> voices.firstOrNull()
            }
            
            if (targetVoice != null) {
                tts?.voice = targetVoice
                currentVoice = targetVoice
                
                // Save preference
                val prefs = context.getSharedPreferences("voice_settings", Context.MODE_PRIVATE)
                prefs.edit().putString("tts_gender", gender).apply()
                
                Log.d(TAG, "✅ Voice changed to: ${targetVoice.name} ($gender)")
            } else {
                Log.w(TAG, "⚠️ No $gender voice found for ${currentLanguage.displayLanguage}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error selecting voice by gender", e)
        }
    }
    
    /**
     * Change voice by exact voice ID
     */
    fun changeVoice(voiceId: String) {
        val voice = getAvailableVoices().find { it.name == voiceId }
        if (voice != null) {
            tts?.voice = voice
            currentVoice = voice
            Log.d(TAG, "✅ Voice changed to: ${voice.name}")
        } else {
            Log.w(TAG, "⚠️ Voice not found: $voiceId")
        }
    }
    
    /**
     * Get all available voices for current language
     */
    fun getAvailableVoices(): Set<Voice> {
        return tts?.voices?.filter {
            it.locale.language == currentLanguage.language && 
            !it.isNetworkConnectionRequired
        }?.toSet() ?: emptySet()
    }
    
    /**
     * Get currently selected voice
     */
    fun getCurrentVoice(): Voice? = currentVoice ?: tts?.voice
    
    /**
     * ✅ NEW: Set speech rate (0.1 to 3.0)
     * Default is 1.0
     * < 1.0 = slower
     * > 1.0 = faster
     */
    fun setSpeechRate(rate: Float) {
        val normalizedRate = rate.coerceIn(0.1f, 3.0f)
        tts?.setSpeechRate(normalizedRate)
        Log.d(TAG, "Speech rate set to: $normalizedRate")
    }
    
    /**
     * ✅ NEW: Set pitch (0.1 to 2.0)
     * Default is 1.0
     * < 1.0 = lower pitch
     * > 1.0 = higher pitch
     */
    fun setPitch(pitch: Float) {
        val normalizedPitch = pitch.coerceIn(0.1f, 2.0f)
        tts?.setPitch(normalizedPitch)
        Log.d(TAG, "Pitch set to: $normalizedPitch")
    }
    
    /**
     * Get list of available voice names with gender info
     */
    fun getVoiceList(): List<Pair<String, String>> {
        return getAvailableVoices().map { voice ->
            val gender = when {
                voice.name.contains("male", ignoreCase = true) && 
                !voice.name.contains("female", ignoreCase = true) -> "Male"
                voice.name.contains("female", ignoreCase = true) -> "Female"
                else -> "Unknown"
            }
            Pair(voice.name, gender)
        }
    }
    
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        Log.d(TAG, "TTS engine shut down")
    }
    
    companion object {
        private const val TAG = "TextToSpeechEngine"
    }
}