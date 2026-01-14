package com.davidstudioz.david.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

/**
 * TextToSpeechEngine - ENHANCED with aggressive filtering
 * ✅ CRITICAL: Removes ALL technical language
 * ✅ Filters: billion, million, thousand, timestamps, paths
 * ✅ Removes: initialized, completed, success, debug terms
 * ✅ Removes: percentages, code syntax, file extensions
 * ✅ Male/female voice selection
 * ✅ Multi-language support
 * ✅ Natural speech only
 */
class TextToSpeechEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private val prefs = context.getSharedPreferences("voice_settings", Context.MODE_PRIVATE)
    
    // Voice settings
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
                    Log.d(TAG, "✅ TTS initialized: language=$currentLanguage, voice=$currentVoice")
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

    /**
     * ✅ CRITICAL: Enhanced filtering - removes ALL technical language
     */
    fun speak(text: String) {
        if (!isTtsInitialized) {
            Log.w(TAG, "TTS not initialized yet")
            return
        }

        val cleanText = filterInternalCode(text)
        if (cleanText.isBlank()) {
            Log.w(TAG, "Filtered text is empty, not speaking")
            return
        }

        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "utterance_id_${System.currentTimeMillis()}")
        Log.d(TAG, "🔊 Speaking: $cleanText")
    }

    /**
     * ✅ CRITICAL: AGGRESSIVE filtering for human-like speech
     */
    private fun filterInternalCode(text: String): String {
        var filtered = text

        // ✅ CRITICAL: Remove number scale words
        filtered = filtered.replace("\\b(billion|bilon|bilion|billon)\\b".toRegex(RegexOption.IGNORE_CASE), "")
        filtered = filtered.replace("\\b(million|milion|millon)\\b".toRegex(RegexOption.IGNORE_CASE), "")
        filtered = filtered.replace("\\b(thousand|thousnd)\\b".toRegex(RegexOption.IGNORE_CASE), "")
        filtered = filtered.replace("\\b(hundred|hundrd)\\b".toRegex(RegexOption.IGNORE_CASE), "")
        
        // ✅ Remove timestamps and time formats
        filtered = filtered.replace("\\d{1,2}:\\d{2}(:\\d{2})?(\\.\\d+)?".toRegex(), "")
        filtered = filtered.replace("\\d{4}-\\d{2}-\\d{2}".toRegex(), "")
        filtered = filtered.replace("\\d{2}/\\d{2}/\\d{4}".toRegex(), "")
        
        // ✅ Remove technical status messages
        val technicalTerms = listOf(
            "initialized", "initializing", "init",
            "completed", "complete", "completion",
            "success", "successful", "successfully",
            "failed", "failure", "error",
            "loading", "loaded", "load",
            "processing", "processed", "process",
            "executing", "executed", "execution",
            "starting", "started", "start",
            "stopping", "stopped", "stop",
            "connecting", "connected", "connection",
            "downloading", "downloaded", "download",
            "uploading", "uploaded", "upload",
            "updating", "updated", "update",
            "configuring", "configured", "config",
            "preparing", "prepared", "prepare",
            "validating", "validated", "validation",
            "verifying", "verified", "verification",
            "detecting", "detected", "detection",
            "analyzing", "analyzed", "analysis"
        )
        
        technicalTerms.forEach { term ->
            filtered = filtered.replace("\\b$term\\b".toRegex(RegexOption.IGNORE_CASE), "")
        }
        
        // ✅ Remove file paths and extensions
        filtered = filtered.replace("/[\\w/.-]+\\.(kt|java|xml|json|txt|log|md)".toRegex(), "")
        filtered = filtered.replace("[A-Z]:[\\\\\\w.-]+".toRegex(), "")
        filtered = filtered.replace("\\.(tflite|gguf|onnx|bin|task|model)".toRegex(RegexOption.IGNORE_CASE), "")
        
        // ✅ Remove percentages and measurements
        filtered = filtered.replace("\\d+\\.?\\d*\\s*%".toRegex(), "")
        filtered = filtered.replace("\\d+\\.?\\d*\\s*(MB|KB|GB|TB)".toRegex(RegexOption.IGNORE_CASE), "")
        filtered = filtered.replace("\\d+\\.?\\d*\\s*(ms|sec|min|hr)".toRegex(RegexOption.IGNORE_CASE), "")
        
        // ✅ Remove code syntax
        filtered = filtered.replace("[{}\\[\\]()<>;:=]".toRegex(), " ")
        filtered = filtered.replace("\\b(true|false|null|undefined)\\b".toRegex(RegexOption.IGNORE_CASE), "")
        
        // ✅ Remove technical symbols
        filtered = filtered.replace("[\u2705\u274c\u26a0\ufe0f\ud83d\udd27\ud83d\udd0a\ud83c\udfa4\ud83d\udd07]".toRegex(), "")
        
        // ✅ Remove debug level indicators
        filtered = filtered.replace("\\b(DEBUG|INFO|WARN|ERROR|FATAL)\\b".toRegex(), "")
        filtered = filtered.replace("\\b(log|logger|logging)\\b".toRegex(RegexOption.IGNORE_CASE), "")
        
        // ✅ Remove common technical prefixes
        filtered = filtered.replace("\\b(await|async|sync|promise)\\b".toRegex(RegexOption.IGNORE_CASE), "")
        filtered = filtered.replace("\\b(function|method|class|object|instance)\\b".toRegex(RegexOption.IGNORE_CASE), "")
        
        // ✅ Remove status indicators
        filtered = filtered.replace("✅", "")
        filtered = filtered.replace("❌", "")
        filtered = filtered.replace("⚠️", "")
        filtered = filtered.replace("⚠", "")
        
        // ✅ Remove multiple spaces and clean up
        filtered = filtered.replace("\\s+".toRegex(), " ")
        filtered = filtered.replace("\\s+([.,!?])".toRegex(), "$1")
        filtered = filtered.trim()
        
        // ✅ Remove sentences that are too short (likely fragments)
        val sentences = filtered.split(".")
        filtered = sentences.filter { it.trim().split(" ").size >= 2 }.joinToString(". ")
        
        return filtered
    }

    fun setLanguage(langCode: String) {
        currentLanguage = langCode
        val locale = getLocaleFromCode(langCode)
        tts?.language = locale
        saveSettings()
        Log.d(TAG, "✅ Language set to: ${locale.displayLanguage}")
    }

    private fun getLocaleFromCode(code: String): Locale {
        return when (code.lowercase()) {
            "en" -> Locale.ENGLISH
            "hi" -> Locale("hi", "IN")
            "es" -> Locale("es", "ES")
            "fr" -> Locale.FRENCH
            "de" -> Locale.GERMAN
            "it" -> Locale.ITALIAN
            "ja" -> Locale.JAPANESE
            "ko" -> Locale.KOREAN
            "zh" -> Locale.CHINESE
            "pt" -> Locale("pt", "BR")
            "ru" -> Locale("ru", "RU")
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
                Log.d(TAG, "✅ Voice changed to: ${targetVoice.name}")
            } else {
                Log.w(TAG, "⚠️ Voice not found: $voiceId")
            }
        }
    }

    private fun applyVoiceSelection() {
        currentVoice?.let { voiceId ->
            changeVoice(voiceId)
        }
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
    }
}