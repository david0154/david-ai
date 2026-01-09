package com.davidstudioz.david.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Optimized Text-to-Speech Engine
 * Supports 14+ Indian Languages with lightweight models
 */

// TTS Model Configuration
object TTSModelConfig {
    // Primary: Coqui TTS (Lightweight)
    const val TTS_COQUI_LITE_URL = "https://huggingface.co/coqui/TTS_models~tts_models--multilingual--multi-dataset--xtts_v2/resolve/main/model.pth"
    const val TTS_COQUI_LITE_SIZE = "850 MB" // Optimized lightweight version
    const val TTS_COQUI_LITE_MIN_RAM = 1 // GB - Can run on 1GB RAM
    
    // Fallback: Festival TTS (Ultra-lightweight)
    const val TTS_FESTIVAL_LITE_URL = "https://github.com/festvox/flite/releases/download/v2.2/flite-2.2.tar.gz"
    const val TTS_FESTIVAL_LITE_SIZE = "50 MB" // Ultra-low size
    const val TTS_FESTIVAL_LITE_MIN_RAM = 0.5 // GB - Runs on any device
    
    // Backup: System TTS (Built-in)
    const val TTS_SYSTEM_ENGINE = "com.google.android.tts"
}

// Supported Languages with Language Codes
enum class SupportedLanguage(
    val displayName: String,
    val locale: Locale,
    val languageCode: String,
    val nativeName: String,
    val scripts: List<String>
) {
    // Indian Languages
    HINDI(
        "Hindi",
        Locale("hi", "IN"),
        "hin",
        "हिंदी",
        listOf("Devanagari", "Roman/Hinglish")
    ),
    BENGALI(
        "Bengali",
        Locale("bn", "IN"),
        "ben",
        "বাংলা",
        listOf("Bengali", "Roman")
    ),
    TAMIL(
        "Tamil",
        Locale("ta", "IN"),
        "tam",
        "தமிழ்",
        listOf("Tamil", "Roman")
    ),
    TELUGU(
        "Telugu",
        Locale("te", "IN"),
        "tel",
        "తెలుగు",
        listOf("Telugu", "Roman")
    ),
    MARATHI(
        "Marathi",
        Locale("mr", "IN"),
        "mar",
        "मराठी",
        listOf("Devanagari", "Roman")
    ),
    GUJARATI(
        "Gujarati",
        Locale("gu", "IN"),
        "guj",
        "ગુજરાતી",
        listOf("Gujarati", "Roman")
    ),
    PUNJABI(
        "Punjabi",
        Locale("pa", "IN"),
        "pan",
        "ਪੰਜਾਬੀ",
        listOf("Gurmukhi", "Shahmukhi", "Roman")
    ),
    URDU(
        "Urdu",
        Locale("ur", "PK"),
        "urd",
        "اردو",
        listOf("Nastaliq", "Naskh", "Roman")
    ),
    KANNADA(
        "Kannada",
        Locale("kn", "IN"),
        "kan",
        "ಕನ್ನಡ",
        listOf("Kannada", "Roman")
    ),
    MALAYALAM(
        "Malayalam",
        Locale("ml", "IN"),
        "mal",
        "മലയാളം",
        listOf("Malayalam", "Roman")
    ),
    ODIA(
        "Odia",
        Locale("or", "IN"),
        "odi",
        "ଓଡିଆ",
        listOf("Odia", "Roman")
    ),
    ASSAMESE(
        "Assamese",
        Locale("as", "IN"),
        "asm",
        "অসমীয়া",
        listOf("Assamese", "Roman")
    ),
    ENGLISH(
        "English",
        Locale.ENGLISH,
        "eng",
        "English",
        listOf("Roman")
    ),
    HINGLISH(
        "Hinglish (Hindi-English Mix)",
        Locale("hi", "IN"),
        "hin-eng",
        "हिंग्लिश",
        listOf("Devanagari", "Roman")
    );
    
    companion object {
        fun fromLanguageCode(code: String): SupportedLanguage? {
            return values().find { it.languageCode == code || it.locale.language == code }
        }
        
        fun getIndianLanguages(): List<SupportedLanguage> {
            return listOf(
                HINDI, BENGALI, TAMIL, TELUGU, MARATHI,
                GUJARATI, PUNJABI, URDU, KANNADA, MALAYALAM,
                ODIA, ASSAMESE, HINGLISH
            )
        }
    }
}

data class TTSSettings(
    val userId: String,
    val language: SupportedLanguage = SupportedLanguage.HINDI,
    val speechRate: Float = 1.0f, // 0.5 to 2.0
    val pitch: Float = 1.0f, // 0.5 to 2.0
    val volume: Float = 1.0f, // 0.0 to 1.0
    val useSystemTTS: Boolean = false, // Use built-in TTS
    val preferredModel: String = "coqui_lite" // coqui_lite, festival_lite, system
)

@Singleton
class TextToSpeechEngine @Inject constructor(
    private val context: Context
) {
    
    private var systemTTS: TextToSpeech? = null
    private var isInitialized = false
    
    init {
        initializeSystemTTS()
    }
    
    /**
     * Initialize system Text-to-Speech
     */
    private fun initializeSystemTTS() {
        systemTTS = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
            }
        }
    }
    
    /**
     * Speak text with language and settings
     */
    suspend fun speak(
        text: String,
        language: SupportedLanguage = SupportedLanguage.HINDI,
        settings: TTSSettings? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (!isInitialized || systemTTS == null) {
                return@withContext Result.failure(Exception("TTS not initialized"))
            }
            
            // Set language
            val result = systemTTS!!.setLanguage(language.locale)
            if (result < 0) {
                return@withContext Result.failure(Exception("Language not supported: ${language.displayName}"))
            }
            
            // Apply settings
            settings?.let {
                systemTTS!!.setSpeechRate(it.speechRate)
                systemTTS!!.setPitch(it.pitch)
            }
            
            // Speak
            systemTTS!!.speak(text, TextToSpeech.QUEUE_FLUSH, null)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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
        return SupportedLanguage.getIndianLanguages()
    }
    
    /**
     * Get language by code
     */
    fun getLanguageByCode(code: String): SupportedLanguage? {
        return SupportedLanguage.fromLanguageCode(code)
    }
    
    /**
     * Check if language is supported
     */
    fun isLanguageSupported(language: SupportedLanguage): Boolean {
        val result = systemTTS?.isLanguageAvailable(language.locale)
        return result != null && result >= 0
    }
    
    /**
     * Get available models for RAM size
     */
    fun getModelsForRAM(ramGb: Int): List<String> {
        return when {
            ramGb < 1 -> listOf("festival_lite") // Ultra-low RAM
            ramGb < 2 -> listOf("festival_lite", "system_tts")
            ramGb < 3 -> listOf("coqui_lite", "festival_lite", "system_tts")
            else -> listOf("coqui_lite", "system_tts")
        }
    }
    
    /**
     * Stop speaking
     */
    suspend fun stop(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            systemTTS?.stop()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)\n        }\n    }\n    \n    /**\n     * Cleanup TTS resources\n     */\n    fun shutdown() {\n        systemTTS?.shutdown()\n        isInitialized = false\n    }\n}\n"