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
 * ✅ 15 languages supported (10 original + 5 new)
 * ✅ English default language
 * ✅ Download additional languages
 * ✅ Switch languages dynamically
 * ✅ Complete language parity with ModelManager
 */
class LanguageManager(private val context: Context) {
    
    private val modelManager = ModelManager(context)
    private val prefs = context.getSharedPreferences("david_language", Context.MODE_PRIVATE)
    
    // All 15 supported languages
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
        Language("pa", "Punjabi", "ਪੰਜਾਬੀ"),
        // NEW: 5 additional languages
        Language("or", "Odia", "ଓଡ଼ିଆ"),
        Language("ur", "Urdu", "اردو"),
        Language("sa", "Sanskrit", "संस्कृतम्"),
        Language("ks", "Kashmiri", "कॉशुर"),
        Language("as", "Assamese", "অসমীয়া")
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
     * Uses multilingual model that supports all languages
     */
    fun isLanguageDownloaded(languageCode: String): Boolean {
        // English is always available (default)
        if (languageCode == "en") return true
        
        // Check if multilingual model is downloaded (supports all languages)
        val multilingualModel = modelManager.getLanguageModelPath()
        return multilingualModel != null && multilingualModel.exists()
    }
    
    /**
     * Download language model
     * Uses the multilingual model that supports all 15 languages
     */
    suspend fun downloadLanguage(
        languageCode: String,
        onProgress: (Int) -> Unit = {}
    ): Result<File> {
        return try {
            val language = supportedLanguages.find { it.code == languageCode }
                ?: return Result.failure(Exception("Language not supported"))
            
            Log.d(TAG, "Downloading language support for: ${language.name}")
            
            // Check if multilingual model is already downloaded
            val existingModel = modelManager.getLanguageModelPath()
            if (existingModel != null && existingModel.exists()) {
                Log.d(TAG, "Multilingual model already downloaded")
                return Result.success(existingModel)
            }
            
            // Download multilingual model (supports all languages)
            val multilingualModel = AIModel(
                "D.A.V.I.D Multilingual",
                "https://huggingface.co/sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2/resolve/main/onnx/model.onnx",
                "120 MB", 1, "Language", "ONNX", "multilingual"
            )
            
            val result = modelManager.downloadModel(multilingualModel, onProgress)
            
            if (result.isSuccess) {
                Log.d(TAG, "Multilingual model downloaded - supports all 15 languages")
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading language", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get downloaded languages
     * If multilingual model is downloaded, all languages are available
     */
    fun getDownloadedLanguages(): List<Language> {
        val multilingualDownloaded = modelManager.getLanguageModelPath()?.exists() == true
        
        return if (multilingualDownloaded) {
            // All languages are available when multilingual model is downloaded
            supportedLanguages.map { it.copy(isDownloaded = true) }
        } else {
            // Only English is available by default
            supportedLanguages.filter { it.code == "en" }
        }
    }
    
    /**
     * Get language by code
     */
    fun getLanguageByCode(code: String): Language? {
        return supportedLanguages.find { it.code == code }
    }
    
    /**
     * Delete language model
     * Note: This deletes the multilingual model affecting all languages
     */
    fun deleteLanguage(languageCode: String): Boolean {
        if (languageCode == "en") {
            Log.w(TAG, "Cannot delete default language (English)")
            return false
        }
        
        return try {
            val modelPath = modelManager.getLanguageModelPath()
            val deleted = modelPath?.delete() ?: false
            
            if (deleted) {
                Log.d(TAG, "Multilingual model deleted (affects all non-English languages)")
                // Switch to English if current language was deleted
                if (getCurrentLanguage() != "en") {
                    setCurrentLanguage("en")
                }
            }
            
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting language model", e)
            false
        }
    }
    
    companion object {
        private const val TAG = "LanguageManager"
    }
}
