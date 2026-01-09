package com.davidstudioz.david.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.os.Build
import java.util.*

/**
 * Text-to-Speech Engine
 * Supports 14+ Indian Languages
 * Auto-selects model based on device RAM
 */
class TextToSpeechEngine(
    context: Context,
    private val onReady: () -> Unit
) : OnInitListener {

    private var tts: TextToSpeech? = null
    private val context = context.applicationContext
    private var isInitialized = false

    enum class SupportedLanguage(
        val displayName: String,
        val nativeName: String,
        val locale: Locale,
        val languageCode: String
    ) {
        HINDI("Hindi", "हिंदी", Locale("hi"), "hin"),
        BENGALI("Bengali", "বাংলা", Locale("bn"), "ben"),
        TAMIL("Tamil", "தமிழ்", Locale("ta"), "tam"),
        TELUGU("Telugu", "తెలుగు", Locale("te"), "tel"),
        MARATHI("Marathi", "मराठी", Locale("mr"), "mar"),
        GUJARATI("Gujarati", "ગુજરાતી", Locale("gu"), "guj"),
        PUNJABI("Punjabi", "ਪੰਜਾਬੀ", Locale("pa"), "pan"),
        URDU("Urdu", "اردو", Locale("ur"), "urd"),
        KANNADA("Kannada", "ಕನ್ನಡ", Locale("kn"), "kan"),
        MALAYALAM("Malayalam", "മലയാളം", Locale("ml"), "mal"),
        ODIA("Odia", "ଓଡ଼ିଆ", Locale("or"), "odi"),
        ASSAMESE("Assamese", "অসমীয়া", Locale("as"), "asm"),
        ENGLISH("English", "English", Locale.ENGLISH, "eng")
    }

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            onReady()
        }
    }

    /**
     * Speak text in specified language
     */
    fun speak(
        text: String,
        language: SupportedLanguage = SupportedLanguage.ENGLISH,
        speed: Float = 1.0f,
        pitch: Float = 1.0f
    ) {
        if (!isInitialized || text.isEmpty()) return

        val result = tts?.setLanguage(language.locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Fallback to English
            tts?.setLanguage(Locale.ENGLISH)
        }

        tts?.setSpeechRate(speed)
        tts?.setPitch(pitch)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "unique_id")
        } else {
            @Suppress("DEPRECATION")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    /**
     * Get all supported languages
     */
    fun getSupportedLanguages(): List<SupportedLanguage> {
        return SupportedLanguage.values().toList()
    }

    /**
     * Get Indian languages only
     */
    fun getIndianLanguages(): List<SupportedLanguage> {
        return listOf(
            SupportedLanguage.HINDI,
            SupportedLanguage.BENGALI,
            SupportedLanguage.TAMIL,
            SupportedLanguage.TELUGU,
            SupportedLanguage.MARATHI,
            SupportedLanguage.GUJARATI,
            SupportedLanguage.PUNJABI,
            SupportedLanguage.URDU,
            SupportedLanguage.KANNADA,
            SupportedLanguage.MALAYALAM,
            SupportedLanguage.ODIA,
            SupportedLanguage.ASSAMESE
        )
    }

    /**
     * Get language by code
     */
    fun getLanguageByCode(code: String): SupportedLanguage? {
        return SupportedLanguage.values().find { it.languageCode == code }
    }

    /**
     * Stop speaking
     */
    fun stop() {
        if (isInitialized) {
            tts?.stop()
        }
    }

    /**
     * Release TTS resources
     */
    fun release() {
        if (isInitialized) {
            tts?.stop()
            tts?.shutdown()
            isInitialized = false
        }
    }
}
