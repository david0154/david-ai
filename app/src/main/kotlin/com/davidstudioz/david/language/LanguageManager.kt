package com.davidstudioz.david.language

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.io.File

/**
 * LanguageManager - Manages multi-language support
 * Connected to: SafeMainActivity, VoiceController, ChatEngine
 */
class LanguageManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("david_language", Context.MODE_PRIVATE)
    private val modelsDir = File(context.filesDir, "david_models")
    
    /**
     * Get all supported languages
     * Called by: SafeMainActivity for language selector
     */
    fun getSupportedLanguages(): List<Language> {
        val languageModel = File(modelsDir, "language_multilingual.bin")
        val isDownloaded = languageModel.exists()
        
        return listOf(
            Language("en", "English", "English", isDownloaded),
            Language("hi", "Hindi", "हिन्दी", isDownloaded),
            Language("ta", "Tamil", "தமிழ்", isDownloaded),
            Language("te", "Telugu", "తెలుగు", isDownloaded),
            Language("bn", "Bengali", "বাংলা", isDownloaded),
            Language("mr", "Marathi", "मराठी", isDownloaded),
            Language("gu", "Gujarati", "ગુજરાતી", isDownloaded),
            Language("kn", "Kannada", "ಕನ್ನಡ", isDownloaded),
            Language("ml", "Malayalam", "മലയാളം", isDownloaded),
            Language("pa", "Punjabi", "ਪੰਜਾਬੀ", isDownloaded),
            Language("or", "Odia", "ଓଡ଼ିଆ", isDownloaded),
            Language("as", "Assamese", "অসমীয়া", isDownloaded),
            Language("ur", "Urdu", "اردو", isDownloaded),
            Language("sa", "Sanskrit", "संस्कृतम्", isDownloaded),
            Language("ne", "Nepali", "नेपाली", isDownloaded)
        )
    }
    
    /**
     * Get downloaded languages
     * Called by: SafeMainActivity for stats display
     */
    fun getDownloadedLanguages(): List<Language> {
        return getSupportedLanguages().filter { it.isDownloaded }
    }
    
    /**
     * Get current active language
     * Called by: VoiceController, ChatEngine
     */
    fun getCurrentLanguage(): Language {
        val code = prefs.getString("current_language", "en") ?: "en"
        return getSupportedLanguages().find { it.code == code } 
            ?: getSupportedLanguages().first()
    }
    
    /**
     * Set active language
     * Called by: SafeMainActivity language selector
     */
    fun setCurrentLanguage(languageCode: String) {
        prefs.edit().putString("current_language", languageCode).apply()
        Log.d(TAG, "Language set to: $languageCode")
    }
    
    /**
     * Get enabled languages for voice recognition
     * Called by: VoiceController
     */
    fun getEnabledLanguages(): List<Language> {
        val enabledCodes = prefs.getStringSet("enabled_languages", setOf("en")) ?: setOf("en")
        return getSupportedLanguages().filter { it.code in enabledCodes && it.isDownloaded }
    }
    
    /**
     * Enable language for voice recognition
     * Called by: SafeMainActivity language selector
     */
    fun enableLanguage(languageCode: String) {
        val enabled = prefs.getStringSet("enabled_languages", setOf("en"))?.toMutableSet() ?: mutableSetOf("en")
        enabled.add(languageCode)
        prefs.edit().putStringSet("enabled_languages", enabled).apply()
        Log.d(TAG, "Language enabled: $languageCode")
    }
    
    /**
     * Disable language for voice recognition
     * Called by: SafeMainActivity language selector
     */
    fun disableLanguage(languageCode: String) {
        val enabled = prefs.getStringSet("enabled_languages", setOf("en"))?.toMutableSet() ?: mutableSetOf("en")
        if (enabled.size > 1) { // Keep at least one language enabled
            enabled.remove(languageCode)
            prefs.edit().putStringSet("enabled_languages", enabled).apply()
            Log.d(TAG, "Language disabled: $languageCode")
        }
    }
    
    companion object {
        private const val TAG = "LanguageManager"
    }
}
