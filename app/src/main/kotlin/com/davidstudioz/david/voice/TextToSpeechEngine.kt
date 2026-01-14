package com.davidstudioz.david.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import java.util.*

/**
 * TextToSpeechEngine - Enhanced with speed, pitch control, male/female voices
 * ✅ Supports multiple voices (Male and Female)
 * ✅ Speed and pitch adjustment
 * ✅ Voice selection (David-Male / Dayana-Female)
 * ✅ Filters internal code and debug messages
 * ✅ Prevents speaking technical strings
 * ✅ FIXED: Only discovers and uses selected voice (male OR female, not both)
 * ✅ FIXED: Enhanced filtering to prevent speaking debug/internal code
 * ✅ FIXED: Multi-language support - speaks in selected language
 * ✅ FIXED: Both male AND female voices properly supported
 */
class TextToSpeechEngine(private val context: Context) {
    
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val prefs = context.getSharedPreferences("voice_settings", Context.MODE_PRIVATE)
    
    // Available voice options
    private var maleVoice: Voice? = null
    private var femaleVoice: Voice? = null
    private var currentVoiceGender: String = "male" // Track current gender
    private var currentLanguage: Locale = Locale.ENGLISH // Track current language
    
    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                
                // ✅ FIXED: Load saved language preference
                val savedLang = prefs.getString("tts_language", "en") ?: "en"
                currentLanguage = getLocaleFromCode(savedLang)
                
                // Set default language
                val result = tts?.setLanguage(currentLanguage)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language $currentLanguage not supported, falling back to English")
                    currentLanguage = Locale.ENGLISH
                    tts?.setLanguage(Locale.ENGLISH)
                } else {
                    Log.d(TAG, "✅ TTS initialized with language: ${currentLanguage.displayLanguage}")
                }
                
                // Load saved voice parameters
                val speed = prefs.getFloat("voice_speed", 1.0f)
                val pitch = prefs.getFloat("voice_pitch", 1.0f)
                setSpeechRate(speed)
                setPitch(pitch)
                
                // ✅ FIXED: Discover both male and female voices at startup
                discoverAllVoices()
                
                // Set voice based on user selection
                setVoiceFromPreferences()
            } else {
                Log.e(TAG, "TTS initialization failed")
                isInitialized = false
            }
        }
    }
    
    /**
     * ✅ NEW: Convert language code to Locale
     */
    private fun getLocaleFromCode(langCode: String): Locale {
        return when (langCode.lowercase()) {
            "en" -> Locale.ENGLISH
            "hi" -> Locale("hi", "IN") // Hindi (India)
            "es" -> Locale("es", "ES") // Spanish
            "fr" -> Locale.FRENCH
            "de" -> Locale.GERMAN
            "it" -> Locale.ITALIAN
            "ja" -> Locale.JAPANESE
            "ko" -> Locale.KOREAN
            "zh" -> Locale.CHINESE
            "pt" -> Locale("pt", "BR") // Portuguese (Brazil)
            "ru" -> Locale("ru", "RU") // Russian
            "ar" -> Locale("ar", "SA") // Arabic
            "bn" -> Locale("bn", "IN") // Bengali
            "ta" -> Locale("ta", "IN") // Tamil
            "te" -> Locale("te", "IN") // Telugu
            "mr" -> Locale("mr", "IN") // Marathi
            "gu" -> Locale("gu", "IN") // Gujarati
            else -> Locale.ENGLISH
        }
    }
    
    /**
     * ✅ FIXED: Discover BOTH male and female voices for current language
     */
    private fun discoverAllVoices() {
        try {
            val voices = tts?.voices ?: return
            Log.d(TAG, "Discovering voices for ${currentLanguage.displayLanguage}... Found ${voices.size} total voices")
            
            // Filter voices for current language
            val languageVoices = voices.filter { voice ->
                voice.locale.language == currentLanguage.language
            }
            
            if (languageVoices.isEmpty()) {
                Log.w(TAG, "No voices found for ${currentLanguage.displayLanguage}, using default")
                return
            }
            
            Log.d(TAG, "Found ${languageVoices.size} voices for ${currentLanguage.displayLanguage}")
            
            // ✅ FIXED: Discover male voice
            maleVoice = languageVoices.firstOrNull { voice ->
                val name = voice.name.lowercase()
                (name.contains("male") && !name.contains("female")) ||
                name.contains("#male") ||
                name.contains("-male") ||
                name.contains("man") ||
                name.contains("guy") ||
                name.contains("alex") ||
                name.contains("daniel") ||
                name.contains("david") ||
                name.contains("thomas") ||
                name.contains("james") ||
                name.contains("#m0")
            } ?: languageVoices.firstOrNull() // Fallback to first available
            
            // ✅ FIXED: Discover female voice
            femaleVoice = languageVoices.firstOrNull { voice ->
                val name = voice.name.lowercase()
                name.contains("female") ||
                name.contains("#female") ||
                name.contains("-female") ||
                name.contains("woman") ||
                name.contains("girl") ||
                name.contains("samantha") ||
                name.contains("victoria") ||
                name.contains("emily") ||
                name.contains("sarah") ||
                name.contains("kate") ||
                name.contains("#f0")
            } ?: languageVoices.lastOrNull() // Fallback to last available
            
            Log.d(TAG, "✅ Male voice: ${maleVoice?.name ?: "Not found"}")
            Log.d(TAG, "✅ Female voice: ${femaleVoice?.name ?: "Not found"}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error discovering voices", e)
        }
    }
    
    /**
     * ✅ FIXED: Speak text with enhanced filtering and language support
     */
    fun speak(text: String) {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS not initialized")
            return
        }
        
        try {
            // Filter out internal code before speaking
            val filteredText = filterInternalCode(text)
            
            if (filteredText.isBlank()) {
                Log.w(TAG, "Text filtered out (internal code detected): $text")
                return
            }
            
            // ✅ FIXED: Ensure language is set before speaking
            tts?.setLanguage(currentLanguage)
            
            tts?.speak(filteredText, TextToSpeech.QUEUE_FLUSH, null, null)
            Log.d(TAG, "Speaking in ${currentLanguage.displayLanguage}: $filteredText")
        } catch (e: Exception) {
            Log.e(TAG, "Error speaking", e)
        }
    }
    
    /**
     * ✅ ENHANCED: Filter internal code and technical strings with stricter rules
     */
    private fun filterInternalCode(text: String): String {
        var filtered = text.trim()
        
        if (filtered.length < 2) return ""
        
        val debugPrefixes = listOf(
            "bilon", "debug", "error", "warn", "info", "log:", "tag:",
            "exception", "stacktrace", "null", "undefined",
            "system", "internal", "code:", "status:",
            "initialized", "loading", "processing"
        )
        
        val lowerFiltered = filtered.lowercase()
        for (prefix in debugPrefixes) {
            if (lowerFiltered.startsWith(prefix) || lowerFiltered.contains(" $prefix")) {
                return ""
            }
        }
        
        val codePatterns = listOf(
            "\\w+\\.\\w+\\.\\w+".toRegex(),
            "\\w+::\\w+".toRegex(),
            "\\w+\\(\\)".toRegex(),
            "\\{.*\\}".toRegex(),
            "\\[.*\\]".toRegex(),
            "<.*>".toRegex(),
            "^\\[\\w+\\]".toRegex(),
            "^\\w+:\\s*".toRegex(),
            "0x[0-9a-fA-F]+".toRegex(),
        )
        
        for (pattern in codePatterns) {
            if (pattern.containsMatchIn(filtered)) return ""
        }
        
        filtered = filtered.replace("[\ud83c\udc00-\ud83e\uddff]".toRegex(), "")
        filtered = filtered.replace("[\u2705\u274c\u26a0\ufe0f\ud83d\udc4d]".toRegex(), "")
        filtered = filtered.replace("\\s+".toRegex(), " ").trim()
        
        if (filtered.length < 3) return ""
        
        return filtered
    }
    
    /**
     * ✅ NEW: Set language for TTS
     */
    fun setLanguage(langCode: String) {
        try {
            currentLanguage = getLocaleFromCode(langCode)
            val result = tts?.setLanguage(currentLanguage)
            
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "Language ${currentLanguage.displayLanguage} not supported")
                currentLanguage = Locale.ENGLISH
                tts?.setLanguage(Locale.ENGLISH)
            } else {
                prefs.edit().putString("tts_language", langCode).apply()
                Log.d(TAG, "Language set to: ${currentLanguage.displayLanguage}")
                
                // Re-discover voices for new language
                discoverAllVoices()
                setVoiceFromPreferences()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting language", e)
        }
    }
    
    /**
     * Set speech rate (0.5 - 2.0)
     */
    fun setSpeechRate(rate: Float) {
        try {
            val clampedRate = rate.coerceIn(0.5f, 2.0f)
            tts?.setSpeechRate(clampedRate)
            prefs.edit().putFloat("voice_speed", clampedRate).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting speech rate", e)
        }
    }
    
    /**
     * Set pitch (0.5 - 2.0)
     */
    fun setPitch(pitch: Float) {
        try {
            val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
            tts?.setPitch(clampedPitch)
            prefs.edit().putFloat("voice_pitch", clampedPitch).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting pitch", e)
        }
    }
    
    /**
     * ✅ FIXED: Set voice based on user preference
     */
    private fun setVoiceFromPreferences() {
        try {
            val selectedVoice = prefs.getString("selected_voice", "david") ?: "david"
            
            val voice = when (selectedVoice.lowercase()) {
                "david", "male" -> {
                    currentVoiceGender = "male"
                    maleVoice
                }
                "dayana", "female" -> {
                    currentVoiceGender = "female"
                    femaleVoice
                }
                else -> maleVoice ?: femaleVoice
            }
            
            voice?.let {
                tts?.voice = it
                Log.d(TAG, "✅ Voice set to: ${it.name} (${currentVoiceGender}, ${currentLanguage.displayLanguage})")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting voice", e)
        }
    }
    
    /**
     * ✅ FIXED: Change voice
     */
    fun changeVoice(voiceId: String) {
        prefs.edit().putString("selected_voice", voiceId).apply()
        currentVoiceGender = if (voiceId.lowercase() in listOf("david", "male")) "male" else "female"
        setVoiceFromPreferences()
    }
    
    /**
     * Get available voice options for UI
     */
    fun getAvailableVoices(): List<VoiceOption> {
        val voices = mutableListOf<VoiceOption>()
        
        if (maleVoice != null) {
            voices.add(VoiceOption(
                id = "david",
                name = "David (Male - ${currentLanguage.displayLanguage})",
                gender = "male",
                isAvailable = true
            ))
        }
        
        if (femaleVoice != null) {
            voices.add(VoiceOption(
                id = "dayana",
                name = "Dayana (Female - ${currentLanguage.displayLanguage})",
                gender = "female",
                isAvailable = true
            ))
        }
        
        if (voices.isEmpty()) {
            voices.add(VoiceOption(
                id = "default",
                name = "Default (${currentLanguage.displayLanguage})",
                gender = "neutral",
                isAvailable = true
            ))
        }
        
        return voices
    }
    
    fun getCurrentVoice(): String = prefs.getString("selected_voice", "david") ?: "david"
    fun getCurrentLanguage(): String = currentLanguage.language
    fun stop() { tts?.stop() }
    
    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
            maleVoice = null
            femaleVoice = null
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        }
    }
    
    data class VoiceOption(
        val id: String,
        val name: String,
        val gender: String,
        val isAvailable: Boolean
    )
    
    companion object {
        private const val TAG = "TextToSpeechEngine"
    }
}