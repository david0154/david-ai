package com.davidstudioz.david.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

/**
 * TextToSpeechEngine - Enhanced with speed, pitch, gender control, and code filtering
 * ✅ Supports multiple voices (male and female)
 * ✅ Speed and pitch adjustment
 * ✅ Voice selection (David/Dayana)
 * ✅ Filters internal code and debug text from speech
 */
class TextToSpeechEngine(private val context: Context) {
    
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val prefs = context.getSharedPreferences("voice_settings", Context.MODE_PRIVATE)
    
    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                
                // Set default language
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported")
                } else {
                    Log.d(TAG, "✅ TTS initialized successfully")
                }
                
                // Load saved voice parameters
                val speed = prefs.getFloat("voice_speed", 1.0f)
                val pitch = prefs.getFloat("voice_pitch", 1.0f)
                setSpeechRate(speed)
                setPitch(pitch)
                
                // Set voice based on selection
                setVoiceFromPreferences()
            } else {
                Log.e(TAG, "TTS initialization failed")
                isInitialized = false
            }
        }
    }
    
    /**
     * ✅ FIXED: Speak text with code filtering
     */
    fun speak(text: String) {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS not initialized")
            return
        }
        
        try {
            // ✅ Filter internal code before speaking
            val cleanText = filterInternalCode(text)
            
            if (cleanText.isBlank()) {
                Log.w(TAG, "Text filtered to empty, not speaking")
                return
            }
            
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, null)
            Log.d(TAG, "Speaking: $cleanText")
        } catch (e: Exception) {
            Log.e(TAG, "Error speaking", e)
        }
    }
    
    /**
     * ✅ NEW: Filter internal code, debug text, and technical terms from speech
     */
    private fun filterInternalCode(text: String): String {
        var filtered = text
        
        // Remove code blocks and syntax
        val codePatterns = listOf(
            "```.*?```".toRegex(RegexOption.DOT_MATCHES_ALL), // Code blocks
            "\\{.*?\\}".toRegex(), // JSON objects
            "\\[.*?\\]".toRegex(), // Arrays
            "<.*?>".toRegex(), // XML/HTML tags
            "\\$\\{.*?\\}".toRegex(), // Template strings
            "function\\s+\\w+".toRegex(), // Functions
            "class\\s+\\w+".toRegex(), // Classes
            "import\\s+.*".toRegex(), // Imports
            "package\\s+.*".toRegex(), // Packages
            "@\\w+".toRegex(), // Annotations
            "//.*".toRegex(), // Comments
            "/\\*.*?\\*/".toRegex(RegexOption.DOT_MATCHES_ALL) // Multi-line comments
        )
        
        for (pattern in codePatterns) {
            filtered = filtered.replace(pattern, "")
        }
        
        // Remove debug/log prefixes and status emojis
        val debugPatterns = listOf(
            "^(DEBUG|INFO|WARN|ERROR|Log\\.d|Log\\.i|Log\\.w|Log\\.e):.*".toRegex(RegexOption.MULTILINE),
            "^\\[.*?\\]\\s*".toRegex(RegexOption.MULTILINE), // [TAG] prefixes
            "✅|❌|⚠️|\ud83d\udca1|\ud83d\ude80|\ud83d\udc4d".toRegex(), // Status emojis
            "✋|✊|☝️|✌️|\ud83d\udc4c".toRegex() // Gesture emojis in text
        )
        
        for (pattern in debugPatterns) {
            filtered = filtered.replace(pattern, "")
        }
        
        // Remove technical syntax
        filtered = filtered
            .replace("\\bval\\s+\\w+\\s*=".toRegex(), "")
            .replace("\\bvar\\s+\\w+\\s*=".toRegex(), "")
            .replace("\\breturn\\s+".toRegex(), "")
            .replace("\\bnull\\b".toRegex(), "")
            .replace("\\bundefined\\b".toRegex(), "")
            .replace("\\.kt\\b".toRegex(), "") // Remove .kt extensions
            .replace("\\.java\\b".toRegex(), "") // Remove .java extensions
        
        // Clean up whitespace
        filtered = filtered
            .replace("\\s+".toRegex(), " ")
            .trim()
        
        return filtered
    }
    
    /**
     * Set speech rate (0.5 - 2.0)
     */
    fun setSpeechRate(rate: Float) {
        try {
            // Clamp rate between 0.5 and 2.0
            val clampedRate = rate.coerceIn(0.5f, 2.0f)
            tts?.setSpeechRate(clampedRate)
            prefs.edit().putFloat("voice_speed", clampedRate).apply()
            Log.d(TAG, "Speech rate set to: $clampedRate")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting speech rate", e)
        }
    }
    
    /**
     * Set pitch (0.5 - 2.0)
     */
    fun setPitch(pitch: Float) {
        try {
            // Clamp pitch between 0.5 and 2.0
            val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
            tts?.setPitch(clampedPitch)
            prefs.edit().putFloat("voice_pitch", clampedPitch).apply()
            Log.d(TAG, "Pitch set to: $clampedPitch")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting pitch", e)
        }
    }
    
    /**
     * ✅ FIXED: Set voice based on user preference with male voice support
     */
    private fun setVoiceFromPreferences() {
        try {
            val selectedVoice = prefs.getString("selected_voice", "david") ?: "david"
            
            // Get available voices
            val voices = tts?.voices ?: return
            
            Log.d(TAG, "Available TTS voices: ${voices.size}")
            voices.forEach { voice ->
                Log.d(TAG, "Voice: ${voice.name}, Locale: ${voice.locale}")
            }
            
            // ✅ Try to find matching voice with gender preference
            val voice = when (selectedVoice) {
                "david" -> {
                    // Try to find male voice
                    Log.d(TAG, "Looking for male voice...")
                    voices.firstOrNull { 
                        it.name.contains("male", ignoreCase = true) && 
                        !it.name.contains("female", ignoreCase = true) &&
                        it.locale.language == "en"
                    } ?: voices.firstOrNull {
                        // Fallback: Use lower pitch voice (typically male)
                        it.locale.language == "en"
                    }?.also {
                        // Set lower pitch for male voice if no explicit male voice found
                        if (!it.name.contains("male", ignoreCase = true)) {
                            setPitch(0.8f) // Lower pitch for male sound
                            Log.d(TAG, "Using default voice with lower pitch for male sound")
                        }
                    }
                }
                "dayana" -> {
                    // Try to find female voice
                    Log.d(TAG, "Looking for female voice...")
                    voices.firstOrNull { 
                        it.name.contains("female", ignoreCase = true) &&
                        it.locale.language == "en"
                    } ?: voices.firstOrNull {
                        // Fallback: Use higher pitch voice (typically female)
                        it.locale.language == "en"
                    }?.also {
                        // Set higher pitch for female voice if no explicit female voice found
                        if (!it.name.contains("female", ignoreCase = true)) {
                            setPitch(1.2f) // Higher pitch for female sound
                            Log.d(TAG, "Using default voice with higher pitch for female sound")
                        }
                    }
                }
                else -> voices.firstOrNull { it.locale.language == "en" }
            }
            
            voice?.let {
                tts?.voice = it
                Log.d(TAG, "✅ Voice set to: ${it.name} (${selectedVoice})")
            } ?: Log.w(TAG, "No suitable voice found for: $selectedVoice")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error setting voice", e)
        }
    }
    
    /**
     * ✅ NEW: Get available voice options
     */
    fun getAvailableVoices(): List<VoiceOption> {
        val voiceOptions = mutableListOf<VoiceOption>()
        
        // Always include David (male) and Dayana (female) as options
        voiceOptions.add(
            VoiceOption(
                id = "david",
                name = "David (Male)",
                gender = "male",
                language = "en-US"
            )
        )
        
        voiceOptions.add(
            VoiceOption(
                id = "dayana",
                name = "Dayana (Female)",
                gender = "female",
                language = "en-US"
            )
        )
        
        return voiceOptions
    }
    
    /**
     * Change voice (call after user changes selection)
     */
    fun changeVoice(voiceId: String) {
        prefs.edit().putString("selected_voice", voiceId).apply()
        setVoiceFromPreferences()
        Log.d(TAG, "Voice changed to: $voiceId")
    }
    
    /**
     * Get current voice selection
     */
    fun getCurrentVoice(): String {
        return prefs.getString("selected_voice", "david") ?: "david"
    }
    
    /**
     * Stop speaking
     */
    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
    }
    
    /**
     * Check if TTS is speaking
     */
    fun isSpeaking(): Boolean {
        return try {
            tts?.isSpeaking ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Cleanup (called automatically)
     */
    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
            Log.d(TAG, "TTS engine shutdown")
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        }
    }
    
    /**
     * Voice option data class
     */
    data class VoiceOption(
        val id: String,
        val name: String,
        val gender: String,
        val language: String
    )
    
    companion object {
        private const val TAG = "TextToSpeechEngine"
    }
}
