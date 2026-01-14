package com.davidstudioz.david.voice

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * VoiceDownloadManager - Download & Install TTS Voices
 * ✅ Download male/female voices for TTS
 * ✅ Support for Google TTS voices
 * ✅ Install premium voices
 * ✅ Multi-language voice download
 */
class VoiceDownloadManager(private val context: Context) {
    
    /**
     * Check if male voice is available
     */
    fun isMaleVoiceAvailable(language: String = "en"): Boolean {
        val tts = TextToSpeech(context, null)
        val locale = getLocaleFromCode(language)
        
        val voices = tts.voices?.filter { voice ->
            voice.locale.language == locale.language &&
            !voice.isNetworkConnectionRequired
        } ?: emptySet()
        
        val hasMale = voices.any { voice ->
            (voice.name.contains("male", ignoreCase = true) &&
             !voice.name.contains("female", ignoreCase = true)) ||
            voice.features?.contains("male") == true
        }
        
        tts.shutdown()
        return hasMale
    }
    
    /**
     * Download Google TTS (includes male voices)
     */
    fun downloadGoogleTTS() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.tts")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Log.d(TAG, "Opening Google TTS download page")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Play Store", e)
        }
    }
    
    /**
     * Open TTS settings to install voices
     */
    fun openTTSSettings() {
        try {
            val intent = Intent("com.android.settings.TTS_SETTINGS").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Log.d(TAG, "Opening TTS settings")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening TTS settings", e)
        }
    }
    
    /**
     * Get list of recommended voice engines
     */
    fun getRecommendedVoiceEngines(): List<VoiceEngineInfo> {
        return listOf(
            VoiceEngineInfo(
                name = "Google Text-to-Speech",
                packageName = "com.google.android.tts",
                description = "High quality male and female voices for multiple languages",
                hasMaleVoice = true,
                languages = listOf("English", "Hindi", "Tamil", "Telugu", "Bengali", "Marathi", "Gujarati", "Kannada", "Malayalam")
            ),
            VoiceEngineInfo(
                name = "Samsung Text-to-Speech",
                packageName = "com.samsung.SMT",
                description = "Premium voices for Samsung devices",
                hasMaleVoice = true,
                languages = listOf("English", "Hindi", "Tamil", "Telugu")
            ),
            VoiceEngineInfo(
                name = "eSpeak TTS",
                packageName = "com.reecedunn.espeak",
                description = "Open-source TTS with male voice support",
                hasMaleVoice = true,
                languages = listOf("English", "Hindi", "Tamil", "Telugu", "Bengali", "Marathi")
            )
        )
    }
    
    /**
     * Check if a voice engine is installed
     */
    fun isVoiceEngineInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Install voice engine from Play Store
     */
    fun installVoiceEngine(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Log.d(TAG, "Opening Play Store for: $packageName")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Play Store", e)
        }
    }
    
    /**
     * Get download instructions for male voice
     */
    fun getMaleVoiceDownloadInstructions(): String {
        return """
        🎤 How to Get Male Voice:
        
        1. Install Google Text-to-Speech:
           - Open Play Store
           - Search "Google Text-to-Speech"
           - Install the app
        
        2. Download Voice Data:
           - Go to Settings → System → Languages & Input
           - Tap "Text-to-Speech output"
           - Tap settings icon next to Google TTS
           - Tap "Install voice data"
           - Download your language (includes male voice)
        
        3. Select Male Voice in D.A.V.I.D:
           - Go to Settings → Voice
           - Select "Male Voice"
           - Done! Voice will sound deeper and masculine
        
        Tip: After installing, restart D.A.V.I.D for best results.
        """.trimIndent()
    }
    
    /**
     * Get available voices info
     */
    suspend fun getAvailableVoicesInfo(): String = withContext(Dispatchers.IO) {
        val tts = TextToSpeech(context, null)
        Thread.sleep(500) // Wait for TTS initialization
        
        val voices = tts.voices?.filter { !it.isNetworkConnectionRequired } ?: emptySet()
        val maleVoices = voices.filter { voice ->
            (voice.name.contains("male", ignoreCase = true) &&
             !voice.name.contains("female", ignoreCase = true)) ||
            voice.features?.contains("male") == true
        }
        val femaleVoices = voices.filter { voice ->
            voice.name.contains("female", ignoreCase = true) ||
            voice.features?.contains("female") == true
        }
        
        val result = buildString {
            appendLine("🔊 Available TTS Voices:")
            appendLine()
            appendLine("Total Voices: ${voices.size}")
            appendLine("Male Voices: ${maleVoices.size}")
            appendLine("Female Voices: ${femaleVoices.size}")
            appendLine()
            
            if (maleVoices.isEmpty()) {
                appendLine("⚠️ No male voices found!")
                appendLine("Recommendation: Install Google TTS")
            } else {
                appendLine("✅ Male voices available:")
                maleVoices.take(3).forEach { voice ->
                    appendLine("  - ${voice.name} (${voice.locale.displayLanguage})")
                }
            }
        }
        
        tts.shutdown()
        return@withContext result
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
            else -> Locale.ENGLISH
        }
    }
    
    companion object {
        private const val TAG = "VoiceDownloadManager"
    }
}

/**
 * Voice Engine Info data class
 * ✅ Renamed from VoiceEngine to avoid conflict with VoiceEngine.kt
 */
data class VoiceEngineInfo(
    val name: String,
    val packageName: String,
    val description: String,
    val hasMaleVoice: Boolean,
    val languages: List<String>
)