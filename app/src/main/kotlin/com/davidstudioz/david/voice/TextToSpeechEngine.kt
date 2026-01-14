package com.davidstudioz.david.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import java.util.*

/**
 * TextToSpeechEngine - FIXED Male/Female Voice Selection
 * ✅ FIXED: Male voice properly applies with pitch adjustment
 * ✅ Better voice detection using features
 * ✅ Speech rate and pitch controls
 * ✅ Multi-language support (15 Indian languages)
 */
class TextToSpeechEngine(private val context: Context) {
    
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var currentLanguage: Locale = Locale.ENGLISH
    private var currentVoice: Voice? = null
    private var currentGender: String = "male"
    
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
        val savedRate = prefs.getFloat("tts_rate", 1.0f)
        val savedPitch = prefs.getFloat("tts_pitch", 1.0f)
        
        setLanguage(savedLang)
        setSpeechRate(savedRate)
        setPitch(savedPitch)
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
        
        // Save preference
        val prefs = context.getSharedPreferences("voice_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("tts_language", langCode).apply()
        
        Log.d(TAG, "Language set to: ${currentLanguage.displayLanguage}")
        
        // Reapply voice selection after language change
        selectVoiceByGender(currentGender)
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
     * ✅ FIXED: Better voice selection with pitch adjustment
     */
    fun selectVoiceByGender(gender: String) {
        if (tts == null || !isInitialized) return
        
        currentGender = gender.lowercase()
        
        try {
            val voices = tts?.voices?.filter { voice ->
                voice.locale.language == currentLanguage.language &&
                !voice.isNetworkConnectionRequired &&
                voice.features != null
            } ?: emptySet()
            
            Log.d(TAG, "Available voices for ${currentLanguage.displayLanguage}: ${voices.size}")
            voices.forEach { voice ->
                Log.d(TAG, "  - ${voice.name} (features: ${voice.features})")
            }
            
            // Try multiple strategies to find the right voice
            val targetVoice = when (currentGender) {
                "male" -> {
                    // Strategy 1: Check features for male indicator
                    voices.firstOrNull { 
                        it.features?.contains("male") == true &&
                        it.features?.contains("female") == false
                    }
                    // Strategy 2: Check name for male (but not female)
                    ?: voices.firstOrNull { 
                        (it.name.contains("male", ignoreCase = true) ||
                         it.name.contains("_male", ignoreCase = true)) &&
                        !it.name.contains("female", ignoreCase = true)
                    }
                    // Strategy 3: Avoid female voices
                    ?: voices.firstOrNull {
                        !it.name.contains("female", ignoreCase = true) &&
                        it.features?.contains("female") != true
                    }
                    // Strategy 4: Any voice
                    ?: voices.firstOrNull()
                }
                "female" -> {
                    // Strategy 1: Check features for female indicator
                    voices.firstOrNull { 
                        it.features?.contains("female") == true
                    }
                    // Strategy 2: Check name for female
                    ?: voices.firstOrNull { 
                        it.name.contains("female", ignoreCase = true)
                    }
                    // Strategy 3: Any voice
                    ?: voices.firstOrNull()
                }
                else -> voices.firstOrNull()
            }
            
            if (targetVoice != null) {
                tts?.voice = targetVoice
                currentVoice = targetVoice
                
                // ✅ FIXED: Apply pitch adjustment for male voice
                when (currentGender) {
                    "male" -> {
                        // Lower pitch for male voice (deeper)
                        setPitch(0.85f)
                        setSpeechRate(0.95f)
                    }
                    "female" -> {
                        // Normal pitch for female voice
                        setPitch(1.0f)
                        setSpeechRate(1.0f)
                    }
                }
                
                // Save preference
                val prefs = context.getSharedPreferences("voice_settings", Context.MODE_PRIVATE)
                prefs.edit().putString("tts_gender", gender).apply()
                
                Log.d(TAG, "✅ Voice changed to: ${targetVoice.name} ($gender, pitch: ${if (currentGender == "male") "0.85" else "1.0"})")
            } else {
                Log.w(TAG, "⚠️ No $gender voice found for ${currentLanguage.displayLanguage}, using default")
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
     * Set speech rate (0.1 to 3.0)
     */
    fun setSpeechRate(rate: Float) {
        val normalizedRate = rate.coerceIn(0.1f, 3.0f)
        tts?.setSpeechRate(normalizedRate)
        
        // Save preference
        val prefs = context.getSharedPreferences("voice_settings", Context.MODE_PRIVATE)
        prefs.edit().putFloat("tts_rate", normalizedRate).apply()
        
        Log.d(TAG, "Speech rate set to: $normalizedRate")
    }
    
    /**
     * Set pitch (0.1 to 2.0)
     */
    fun setPitch(pitch: Float) {
        val normalizedPitch = pitch.coerceIn(0.1f, 2.0f)
        tts?.setPitch(normalizedPitch)
        
        // Save preference
        val prefs = context.getSharedPreferences("voice_settings", Context.MODE_PRIVATE)
        prefs.edit().putFloat("tts_pitch", normalizedPitch).apply()
        
        Log.d(TAG, "Pitch set to: $normalizedPitch")
    }
    
    /**
     * Get list of available voice names with gender info
     */
    fun getVoiceList(): List<Pair<String, String>> {
        return getAvailableVoices().map { voice ->
            val gender = when {
                voice.features?.contains("male") == true && 
                voice.features?.contains("female") == false -> "Male"
                voice.features?.contains("female") == true -> "Female"
                voice.name.contains("male", ignoreCase = true) && 
                !voice.name.contains("female", ignoreCase = true) -> "Male"
                voice.name.contains("female", ignoreCase = true) -> "Female"
                else -> "Neutral"
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