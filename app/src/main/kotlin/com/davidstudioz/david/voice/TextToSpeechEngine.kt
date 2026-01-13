package com.davidstudioz.david.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

/**
 * TextToSpeechEngine - Enhanced with male/female voice support
 * ✅ Male voice (David)
 * ✅ Female voice (Dayana) 
 * ✅ Speed and pitch control
 * ✅ Filters internal code before speaking
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
     * ✅ Speak text with internal code filtering
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
                Log.w(TAG, "Text filtered to empty, skipping TTS")
                return
            }
            
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, null)
            Log.d(TAG, "Speaking: $cleanText")
        } catch (e: Exception) {
            Log.e(TAG, "Error speaking", e)
        }
    }
    
    /**
     * ✅ Filter internal code, debug text, and technical terms
     */
    private fun filterInternalCode(text: String): String {
        var filtered = text
        
        // Remove code blocks and technical syntax
        val codePatterns = listOf(
            "```.*?```".toRegex(RegexOption.DOT_MATCHES_ALL), // Code blocks
            "\\{.*?\\}".toRegex(), // JSON objects
            "<.*?>".toRegex(), // XML/HTML tags
            "\\$\\{.*?\\}".toRegex(), // Template strings
            "function\\s+\\w+\\s*\\(".toRegex(), // Function declarations
            "class\\s+\\w+".toRegex(), // Class declarations
            "import\\s+.*".toRegex(), // Import statements
            "package\\s+.*".toRegex(), // Package declarations
            "@\\w+".toRegex(), // Annotations
            "//.*".toRegex(), // Single line comments
            "/\\*.*?\\*/".toRegex(RegexOption.DOT_MATCHES_ALL) // Multi-line comments
        )
        
        for (pattern in codePatterns) {
            filtered = filtered.replace(pattern, "")
        }
        
        // Remove debug/log prefixes and status emojis
        val debugPatterns = listOf(
            "^(DEBUG|INFO|WARN|ERROR|Log\\.d|Log\\.i|Log\\.w|Log\\.e):.*".toRegex(RegexOption.MULTILINE),
            "^\\[.*?\\]\\s*".toRegex(RegexOption.MULTILINE), // [TAG] prefixes
            "✅|❌|⚠️|💡|🔧|🌐|📱".toRegex() // Status emojis
        )
        
        for (pattern in debugPatterns) {
            filtered = filtered.replace(pattern, "")
        }
        
        // Remove technical variable syntax
        filtered = filtered
            .replace("\\bval\\s+\\w+\\s*=".toRegex(), "")
            .replace("\\bvar\\s+\\w+\\s*=".toRegex(), "")
            .replace("\\breturn\\s+".toRegex(), "")
            .replace("\\bnull\\b".toRegex(), "")
            .replace("\\bundefined\\b".toRegex(), "")
            .replace("\\bBoolean\\b".toRegex(), "")
            .replace("\\bString\\b".toRegex(), "")
            .replace("\\bInt\\b".toRegex(), "")
            .replace("\\bFloat\\b".toRegex(), "")
        
        // Clean up multiple spaces and newlines
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
            val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
            tts?.setPitch(clampedPitch)
            prefs.edit().putFloat("voice_pitch", clampedPitch).apply()
            Log.d(TAG, "Pitch set to: $clampedPitch")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting pitch", e)
        }
    }
    
    /**
     * ✅ Set voice based on user preference with male/female support
     */
    private fun setVoiceFromPreferences() {
        try {
            val selectedVoice = prefs.getString("selected_voice", "david") ?: "david"
            
            // Get available voices
            val voices = tts?.voices ?: return
            
            Log.d(TAG, "Available voices: ${voices.size}")
            voices.forEach {
                Log.d(TAG, "Voice: ${it.name}, Locale: ${it.locale}")
            }
            
            // ✅ Try to find matching voice
            val voice = when (selectedVoice) {
                "david" -> {
                    // ✅ Male voice options
                    voices.firstOrNull { 
                        it.name.contains("male", ignoreCase = true) && 
                        !it.name.contains("female", ignoreCase = true) &&
                        it.locale.language == "en"
                    } ?: voices.firstOrNull {
                        // Fallback: lower pitch for male-like voice
                        it.locale.language == "en"
                    }?.also {
                        // Apply lower pitch for male-like effect
                        setPitch(0.85f)
                    }
                }
                "dayana" -> {
                    // ✅ Female voice options
                    voices.firstOrNull { 
                        it.name.contains("female", ignoreCase = true) &&
                        it.locale.language == "en"
                    } ?: voices.firstOrNull {
                        // Fallback: higher pitch for female-like voice
                        it.locale.language == "en"
                    }?.also {
                        // Apply higher pitch for female-like effect
                        setPitch(1.15f)
                    }
                }
                else -> voices.firstOrNull()
            }
            
            voice?.let {
                tts?.voice = it
                Log.d(TAG, "✅ Voice set to: ${it.name} (${selectedVoice})")
            } ?: Log.w(TAG, "Could not find suitable voice for: $selectedVoice")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error setting voice", e)
        }
    }
    
    /**
     * ✅ Change voice (David = male, Dayana = female)
     */
    fun changeVoice(voiceId: String) {
        prefs.edit().putString("selected_voice", voiceId).apply()
        setVoiceFromPreferences()
        
        // Announce voice change
        val voiceName = if (voiceId == "david") "David" else "Dayana"
        speak("Voice changed to $voiceName")
    }
    
    /**
     * Get available voice options
     */
    fun getAvailableVoices(): List<VoiceOption> {
        return listOf(
            VoiceOption("david", "David", "Male voice"),
            VoiceOption("dayana", "Dayana", "Female voice")
        )
    }
    
    /**
     * Get current voice
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
     * Check if speaking
     */
    fun isSpeaking(): Boolean {
        return tts?.isSpeaking ?: false
    }
    
    /**
     * Cleanup
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
    
    data class VoiceOption(
        val id: String,
        val name: String,
        val description: String
    )
    
    companion object {
        private const val TAG = "TextToSpeechEngine"
    }
}