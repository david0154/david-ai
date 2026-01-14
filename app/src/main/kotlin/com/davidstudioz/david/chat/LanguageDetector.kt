package com.davidstudioz.david.chat

import android.util.Log

/**
 * LanguageDetector - Detects language from text input
 * ✅ Supports 15 Indian languages
 * ✅ Script-based detection
 * ✅ Keyword-based fallback
 */
class LanguageDetector {
    
    fun detectLanguage(text: String): String {
        if (text.isBlank()) return "en"
        
        // Check for specific scripts
        val language = when {
            // Devanagari script (Hindi, Marathi, Sanskrit)
            text.any { it in '\u0900'..'\u097F' } -> detectDevanagari(text)
            
            // Bengali script
            text.any { it in '\u0980'..'\u09FF' } -> "bn"
            
            // Tamil script
            text.any { it in '\u0B80'..'\u0BFF' } -> "ta"
            
            // Telugu script
            text.any { it in '\u0C00'..'\u0C7F' } -> "te"
            
            // Gujarati script
            text.any { it in '\u0A80'..'\u0AFF' } -> "gu"
            
            // Kannada script
            text.any { it in '\u0C80'..'\u0CFF' } -> "kn"
            
            // Malayalam script
            text.any { it in '\u0D00'..'\u0D7F' } -> "ml"
            
            // Punjabi script (Gurmukhi)
            text.any { it in '\u0A00'..'\u0A7F' } -> "pa"
            
            // Odia script
            text.any { it in '\u0B00'..'\u0B7F' } -> "or"
            
            // Urdu (uses Arabic script)
            text.any { it in '\u0600'..'\u06FF' } -> "ur"
            
            // English (default)
            else -> "en"
        }
        
        Log.d(TAG, "Detected language: $language for text: ${text.take(50)}")
        return language
    }
    
    private fun detectDevanagari(text: String): String {
        val lower = text.lowercase()
        return when {
            // Sanskrit indicators
            lower.contains("sanskrit") || lower.contains("संस्कृत") -> "sa"
            // Marathi indicators
            lower.contains("marathi") || lower.contains("मराठी") -> "mr"
            // Default to Hindi for Devanagari
            else -> "hi"
        }
    }
    
    fun getLanguageName(code: String): String {
        return when (code) {
            "en" -> "English"
            "hi" -> "Hindi (हिन्दी)"
            "bn" -> "Bengali (বাংলা)"
            "ta" -> "Tamil (தமிழ்)"
            "te" -> "Telugu (తెలుగు)"
            "mr" -> "Marathi (मराठी)"
            "gu" -> "Gujarati (ગુજરાતી)"
            "kn" -> "Kannada (ಕನ್ನಡ)"
            "ml" -> "Malayalam (മലയാളം)"
            "pa" -> "Punjabi (ਪੰਜਾਬੀ)"
            "or" -> "Odia (ଓଡ଼ିଆ)"
            "ur" -> "Urdu (اردو)"
            "as" -> "Assamese (অসমীয়া)"
            "ks" -> "Kashmiri (کٲشُر)"
            "sa" -> "Sanskrit (संस्कृत)"
            else -> "English"
        }
    }
    
    companion object {
        private const val TAG = "LanguageDetector"
    }
}