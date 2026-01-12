package com.davidstudioz.david.language

import android.content.Context
import android.util.Log
import com.davidstudioz.david.models.AIModel
import com.davidstudioz.david.models.ModelManager
import java.io.File

data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    val isDownloaded: Boolean = false,
    val isDefault: Boolean = false
)

/**
 * LanguageManager - Multi-language support
 * ✅ 10 languages supported
 * ✅ English default language
 * ✅ Download additional languages
 * ✅ Switch languages dynamically
 * ✅ Compilation error fixed
 */
class LanguageManager(private val context: Context) {
    
    private val modelManager = ModelManager(context)
    private val prefs = context.getSharedPreferences("david_language", Context.MODE_PRIVATE)
    
    // Supported languages
    private val supportedLanguages = listOf(
        Language("en", "English", "English", isDownloaded = true, isDefault = true),
        Language("hi", "Hindi", "हिंदी"),
        Language("ta", "Tamil", "தமிழ்"),
        Language("te", "Telugu", "తెలుగు"),
        Language("bn", "Bengali", "বাংলা"),
        Language("mr", "Marathi", "मराठी"),
        Language("gu", "Gujarati", "ગુજરાતી"),
        Language("kn", "Kannada", "ಕನ್ನಡ"),
        Language("ml", "Malayalam", "മലയാളം"),
        Language("pa", "Punjabi", "ਪੰਜਾਬੀ")
    )
    
    /**
     * Get all supported languages
     */
    fun getSupportedLanguages(): List<Language> {
        return supportedLanguages.map { lang ->
            lang.copy(isDownloaded = isLanguageDownloaded(lang.code))
        }
    }
    
    /**
     * Get current language
     */
    fun getCurrentLanguage(): String {
        return prefs.getString("current_language", "en") ?: "en"
    }
    
    /**
     * Set current language
     */
    fun setCurrentLanguage(languageCode: String): Boolean {
        return try {
            if (supportedLanguages.any { it.code == languageCode }) {
                prefs.edit().putString("current_language", languageCode).apply()
                Log.d(TAG, "Language set to: $languageCode")
                true
            } else {
                Log.e(TAG, "Language not supported: $languageCode")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting language", e)
            false
        }
    }
    
    /**
     * Check if language model is downloaded
     */
    fun isLanguageDownloaded(languageCode: String): Boolean {
        // English is always available (default)
        if (languageCode == "en") return true
        
        val modelPath = modelManager.getModelPath("language", languageCode)
        return modelPath != null && modelPath.exists()
    }
    
    /**
     * Download language model
     */
    suspend fun downloadLanguage(
        languageCode: String,
        onProgress: (Int) -> Unit = {}
    ): Result<File> {
        return try {
            val language = supportedLanguages.find { it.code == languageCode }
                ?: return Result.failure(Exception("Language not supported"))
            
            Log.d(TAG, "Downloading language: ${language.name}")
            
            val model = modelManager.getLanguageModel(language.name)
                ?: return Result.failure(Exception("Model not found for ${language.name}"))
            
            val result = modelManager.downloadModel(model, onProgress)
            
            if (result.isSuccess) {
                Log.d(TAG, "Language downloaded: ${language.name}")
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading language", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get downloaded languages
     */
    fun getDownloadedLanguages(): List<Language> {
        return supportedLanguages.filter { isLanguageDownloaded(it.code) }
    }
    
    /**
     * Get language by code
     */
    fun getLanguageByCode(code: String): Language? {
        return supportedLanguages.find { it.code == code }
    }
    
    /**
     * Delete language model
     */
    fun deleteLanguage(languageCode: String): Boolean {
        if (languageCode == "en") {
            Log.w(TAG, "Cannot delete default language (English)")
            return false
        }
        
        return try {
            val modelPath = modelManager.getModelPath("language", languageCode)
            val deleted = modelPath?.delete() ?: false
            
            if (deleted) {
                Log.d(TAG, "Language deleted: $languageCode")
                // Switch to English if current language was deleted
                if (getCurrentLanguage() == languageCode) {
                    setCurrentLanguage("en")
                }
            }
            
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting language", e)
            false
        }
    }
    
    companion object {
        private const val TAG = "LanguageManager"
    }
}
