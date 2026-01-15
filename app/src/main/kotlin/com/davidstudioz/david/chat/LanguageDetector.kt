package com.davidstudioz.david.chat

import android.util.Log

/**
 * LanguageDetector - Detects language from text input
 * ✅ Supports 15 Indian languages
 * ✅ Script-based detection
 * ✅ Keyword-based fallback
 */
class LanguageDetector {
    private val languageKeywords = mapOf(
        "hi" to listOf("क्या", "है", "और", "में", "से", "नमस्ते"),
        "bn" to listOf("কি", "এবং", "থেকে", "নমস্কার"),
        "ta" to listOf("என்ன", "மற்றும்", "இருந்து", "வணக்கம்"),
        "te" to listOf("ఏమిటి", "మరియు", "నుండి", "నమస్కారం"),
        "mr" to listOf("काय", "आणि", "पासून", "नमस्कार"),
        "gu" to listOf("શું", "અને", "માંથી", "નમસ્તે"),
        "kn" to listOf("ಏನು", "ಮತ್ತು", "ಇಂದ", "ನಮಸ್ಕಾರ"),
        "ml" to listOf("എന്ത്", "കൂടാതെ", "നിന്ന്", "നമസ്കാരം"),
        "pa" to listOf("ਕੀ", "ਅਤੇ", "ਤੋਂ", "ਸਤ ਸ੍ਰੀ ਅਕਾਲ"),
        "or" to listOf("କଣ", "ଏବଂ", "ଠାରୁ", "ନମସ୍କାର"),
        "ur" to listOf("کیا", "اور", "سے", "آداب"),
        "as" to listOf("কি", "আৰু", "পৰা", "নমস্কাৰ"),
        "ks" to listOf("کیاہ", " تہٕ", "پؠٹھ", "آدا̄ب"),
        "sa" to listOf("किम्", "च", "तः", "नमस्ते")
    )

    fun detectLanguage(text: String): String {
        if (text.isBlank()) return "en"

        // Check for specific scripts
        val scriptBasedLanguage = when {
            text.any { it in '\u0900'..'\u097F' } -> detectDevanagari(text)
            text.any { it in '\u0980'..'\u09FF' } -> "bn"
            text.any { it in '\u0B80'..'\u0BFF' } -> "ta"
            text.any { it in '\u0C00'..'\u0C7F' } -> "te"
            text.any { it in '\u0A80'..'\u0AFF' } -> "gu"
            text.any { it in '\u0C80'..'\u0CFF' } -> "kn"
            text.any { it in '\u0D00'..'\u0D7F' } -> "ml"
            text.any { it in '\u0A00'..'\u0A7F' } -> "pa"
            text.any { it in '\u0B00'..'\u0B7F' } -> "or"
            text.any { it in '\u0600'..'\u06FF' } -> "ur"
            else -> "en"
        }

        val keywordBasedLanguage = detectLanguageWithKeywords(text)
        if (keywordBasedLanguage != "en") {
            return keywordBasedLanguage
        }

        Log.d(TAG, "Detected language: $scriptBasedLanguage for text: ${text.take(50)}")
        return scriptBasedLanguage
    }

    private fun detectLanguageWithKeywords(text: String): String {
        val lowerText = text.lowercase()
        for ((lang, keywords) in languageKeywords) {
            if (keywords.any { it in lowerText }) {
                return lang
            }
        }
        return "en"
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