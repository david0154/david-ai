package com.davidstudioz.david.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

/**
 * TextToSpeechEngine - 15 INDIAN LANGUAGES + Enhanced Filtering
 * ✅ 15 Indian languages (Hindi, Tamil, Telugu, Bengali, etc.)
 * ✅ Male/female voice selection
 * ✅ AGGRESSIVE filtering (no technical jargon)
 * ✅ Natural speech only
 */
class TextToSpeechEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private val prefs = context.getSharedPreferences("voice_settings", Context.MODE_PRIVATE)
    
    private var currentLanguage = "en"
    private var currentVoice: String? = null

    init {
        initializeTTS()
        loadSettings()
    }

    private fun initializeTTS() {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                val result = engine.setLanguage(getLocaleFromCode(currentLanguage))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported: $currentLanguage")
                } else {
                    isTtsInitialized = true
                    applyVoiceSelection()
                    Log.d(TAG, "✅ TTS initialized: ${getLocaleFromCode(currentLanguage).displayLanguage}")
                }
            }
        } else {
            Log.e(TAG, "❌ TTS initialization failed")
        }
    }

    private fun loadSettings() {
        currentLanguage = prefs.getString("tts_language", "en") ?: "en"
        currentVoice = prefs.getString("tts_voice", null)
    }

    private fun saveSettings() {
        prefs.edit()
            .putString("tts_language", currentLanguage)
            .putString("tts_voice", currentVoice)
            .apply()
    }

    fun speak(text: String) {
        if (!isTtsInitialized) {
            Log.w(TAG, "TTS not initialized yet")
            return
        }

        val cleanText = filterInternalCode(text)
        if (cleanText.isBlank()) {
            Log.w(TAG, "Filtered text is empty")
            return
        }

        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "utterance_${System.currentTimeMillis()}")
        Log.d(TAG, "🔊 Speaking: $cleanText")
    }

    private fun filterInternalCode(text: String): String {
        var filtered = text
        filtered = filtered.replace("\\b(billion|bilon|bilion|trillion|million|thousand|hundred)\\b".toRegex(RegexOption.IGNORE_CASE), "")
        filtered = filtered.replace("\\d{1,2}:\\d{2}(:\\d{2})?(\\.\\d+)?".toRegex(), "")
        filtered = filtered.replace("\\d{4}-\\d{2}-\\d{2}".toRegex(), "")
        val technicalTerms = listOf(
            "initialized", "completed", "success", "failed", "error", "loading", "loaded",
            "processing", "executing", "starting", "stopping", "downloading", "uploading"
        )
        technicalTerms.forEach { term ->
            filtered = filtered.replace("\\b$term\\b".toRegex(RegexOption.IGNORE_CASE), "")
        }
        filtered = filtered.replace("/[\\w/.-]+\\.(kt|java|xml|json)".toRegex(), "")
        filtered = filtered.replace("\\.(tflite|gguf|onnx|bin)".toRegex(RegexOption.IGNORE_CASE), "")
        filtered = filtered.replace("\\d+\\.?\\d*\\s*%".toRegex(), "")
        filtered = filtered.replace("[{}\\[\\]()<>;:=]".toRegex(), " ")
        filtered = filtered.replace("[✅❌⚠️⚠]".toRegex(), "")
        filtered = filtered.replace("\\s+".toRegex(), " ")
        filtered = filtered.trim()
        return filtered
    }

    fun setLanguage(langCode: String) {
        currentLanguage = langCode
        val locale = getLocaleFromCode(langCode)
        tts?.language = locale
        saveSettings()
        Log.d(TAG, "✅ Language: ${locale.displayLanguage}")
    }

    /**
     * ✅ COMPLETE: 15 Indian Languages Support
     */
    private fun getLocaleFromCode(code: String): Locale {
        return when (code.lowercase()) {
            "en" -> Locale.ENGLISH
            "hi" -> Locale("hi", "IN")      // Hindi (हिन्दी)
            "ta" -> Locale("ta", "IN")      // Tamil (தமிழ்)
            "te" -> Locale("te", "IN")      // Telugu (తెలుగు)
            "bn" -> Locale("bn", "IN")      // Bengali (বাংলা)
            "mr" -> Locale("mr", "IN")      // Marathi (मराठी)
            "gu" -> Locale("gu", "IN")      // Gujarati (ગુજરાતી)
            "kn" -> Locale("kn", "IN")      // Kannada (ಕನ್ನಡ)
            "ml" -> Locale("ml", "IN")      // Malayalam (മലയാളം)
            "pa" -> Locale("pa", "IN")      // Punjabi (ਪੰਜਾਬੀ)
            "or" -> Locale("or", "IN")      // Odia (ଓଡ଼ିଆ)
            "ur" -> Locale("ur", "IN")      // Urdu (اردو)
            "sa" -> Locale("sa", "IN")      // Sanskrit (संस्कृतम्)
            "ks" -> Locale("ks", "IN")      // Kashmiri (कॉशुर)
            "as" -> Locale("as", "IN")      // Assamese (অসমীয়া)
            else -> Locale.ENGLISH
        }
    }

    fun changeVoice(voiceId: String) {
        tts?.let { engine ->
            val voices = engine.voices ?: return
            val targetVoice = voices.find { voice ->
                voice.name.lowercase().contains(voiceId.lowercase())
            }
            if (targetVoice != null) {
                engine.voice = targetVoice
                currentVoice = voiceId
                saveSettings()
                Log.d(TAG, "✅ Voice: ${targetVoice.name}")
            }
        }
    }

    private fun applyVoiceSelection() {
        currentVoice?.let { changeVoice(it) }
    }

    fun getAvailableVoices(): List<VoiceInfo> {
        val voices = mutableListOf<VoiceInfo>()
        tts?.voices?.forEach { voice ->
            val gender = when {
                voice.name.lowercase().contains("male") && !voice.name.lowercase().contains("female") -> "male"
                voice.name.lowercase().contains("female") -> "female"
                else -> "unknown"
            }
            voices.add(VoiceInfo(voice.name, voice.locale.displayLanguage, gender))
        }
        return voices
    }

    fun getCurrentVoice(): String? = currentVoice

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        isTtsInitialized = false
    }

    data class VoiceInfo(val id: String, val language: String, val gender: String)

    companion object {
        private const val TAG = "TextToSpeechEngine"
        
        /**
         * Get list of supported languages
         */
        fun getSupportedLanguages(): List<LanguageInfo> {
            return listOf(
                LanguageInfo("en", "English", "🇬🇧 English (default)"),
                LanguageInfo("hi", "Hindi", "🇮🇳 Hindi (हिन्दी)"),
                LanguageInfo("ta", "Tamil", "🇮🇳 Tamil (தமிழ்)"),
                LanguageInfo("te", "Telugu", "🇮🇳 Telugu (తెలుగు)"),
                LanguageInfo("bn", "Bengali", "🇮🇳 Bengali (বাংলা)"),
                LanguageInfo("mr", "Marathi", "🇮🇳 Marathi (मराठी)"),
                LanguageInfo("gu", "Gujarati", "🇮🇳 Gujarati (ગુજરાતી)"),
                LanguageInfo("kn", "Kannada", "🇮🇳 Kannada (ಕನ್ನಡ)"),
                LanguageInfo("ml", "Malayalam", "🇮🇳 Malayalam (മലയാളം)"),
                LanguageInfo("pa", "Punjabi", "🇮🇳 Punjabi (ਪੰਜਾਬੀ)"),
                LanguageInfo("or", "Odia", "🇮🇳 Odia (ଓଡ଼ିଆ)"),
                LanguageInfo("ur", "Urdu", "🇮🇳 Urdu (اردو)"),
                LanguageInfo("sa", "Sanskrit", "🇮🇳 Sanskrit (संस्कृतम्)"),
                LanguageInfo("ks", "Kashmiri", "🇮🇳 Kashmiri (कॉशुर)"),
                LanguageInfo("as", "Assamese", "🇮🇳 Assamese (অসমীয়া)")
            )
        }
    }
    
    data class LanguageInfo(val code: String, val name: String, val displayName: String)
}