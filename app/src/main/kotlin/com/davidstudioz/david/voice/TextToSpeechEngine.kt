package com.davidstudioz.david.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import java.util.*

/**
 * TextToSpeechEngine - Enhanced with speed, pitch control, male/female voices
 * ✅ Supports multiple voices (Male and Female)
 * ✅ Speed and pitch adjustment
 * ✅ Voice selection (David-Male / Dayana-Female)
 * ✅ Filters internal code and debug messages
 * ✅ Prevents speaking technical strings
 * ✅ FIXED: Only discovers and uses selected voice (male OR female, not both)
 */
class TextToSpeechEngine(private val context: Context) {
    
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val prefs = context.getSharedPreferences("voice_settings", Context.MODE_PRIVATE)
    
    // Available voice options
    private var maleVoice: Voice? = null
    private var femaleVoice: Voice? = null
    private var currentVoiceGender: String = "male" // Track current gender
    
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
                
                // ✅ FIXED: Set voice based on user selection (only discover what's needed)
                setVoiceFromPreferences()
            } else {
                Log.e(TAG, "TTS initialization failed")
                isInitialized = false
            }
        }
    }
    
    /**
     * ✅ FIXED: Discover only the requested voice gender
     */
    private fun discoverVoice(gender: String): Voice? {
        try {
            val voices = tts?.voices ?: return null
            Log.d(TAG, "Discovering $gender voice... Found ${voices.size} total voices")
            
            val targetVoice = if (gender == "male") {
                // Find male voices
                voices.firstOrNull { voice ->
                    val name = voice.name.lowercase()
                    val locale = voice.locale.toString().lowercase()
                    
                    // Look for male indicators
                    (name.contains("male") && !name.contains("female")) ||
                    name.contains("#male") ||
                    name.contains("-male") ||
                    name.contains("man") ||
                    name.contains("guy") ||
                    // Common male voice names
                    name.contains("alex") ||
                    name.contains("daniel") ||
                    name.contains("david") ||
                    name.contains("thomas") ||
                    name.contains("james") ||
                    (locale.contains("en") && name.contains("#m"))
                } ?: voices.firstOrNull { it.locale.language == "en" && !it.name.lowercase().contains("female") }
            } else {
                // Find female voices
                voices.firstOrNull { voice ->
                    val name = voice.name.lowercase()
                    val locale = voice.locale.toString().lowercase()
                    
                    // Look for female indicators
                    name.contains("female") ||
                    name.contains("#female") ||
                    name.contains("-female") ||
                    name.contains("woman") ||
                    name.contains("girl") ||
                    // Common female voice names
                    name.contains("samantha") ||
                    name.contains("victoria") ||
                    name.contains("emily") ||
                    name.contains("sarah") ||
                    name.contains("kate") ||
                    (locale.contains("en") && name.contains("#f"))
                } ?: voices.lastOrNull { it.locale.language == "en" && !it.name.lowercase().contains("male") }
            }
            
            // Fallback to any English voice
            val result = targetVoice ?: voices.firstOrNull { it.locale.language == "en" }
            
            Log.d(TAG, "✅ ${gender.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} voice: ${result?.name ?: "Not found"}")
            return result
            
        } catch (e: Exception) {
            Log.e(TAG, "Error discovering $gender voice", e)
            return null
        }
    }
    
    /**
     * ✅ FIXED: Speak text with filtering
     */
    fun speak(text: String) {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS not initialized")
            return
        }
        
        try {
            // ✅ FIXED: Filter out internal code before speaking
            val filteredText = filterInternalCode(text)
            
            if (filteredText.isBlank()) {
                Log.w(TAG, "Text filtered out (internal code detected): $text")
                return
            }
            
            tts?.speak(filteredText, TextToSpeech.QUEUE_FLUSH, null, null)
            Log.d(TAG, "Speaking: $filteredText")
        } catch (e: Exception) {
            Log.e(TAG, "Error speaking", e)
        }
    }
    
    /**
     * ✅ NEW: Filter internal code and technical strings
     */
    private fun filterInternalCode(text: String): String {
        var filtered = text.trim()
        
        // Remove if it looks like code or technical output
        val codePatterns = listOf(
            // Programming patterns
            "\\w+\\.\\w+\\.\\w+".toRegex(), // Package names (com.example.app)
            "\\w+::\\w+".toRegex(), // Method references (Class::method)
            "\\w+\\(\\)".toRegex(), // Function calls with empty params
            "\\{.*\\}".toRegex(), // JSON/code blocks
            "\\[.*\\]".toRegex(), // Arrays/lists
            "<.*>".toRegex(), // XML/HTML tags
            
            // Debug/log patterns
            "^\\[\\w+\\]".toRegex(), // [TAG] patterns
            "^\\w+:\\s*".toRegex(), // TAG: patterns at start
            "^DEBUG".toRegex(RegexOption.IGNORE_CASE),
            "^ERROR".toRegex(RegexOption.IGNORE_CASE),
            "^INFO".toRegex(RegexOption.IGNORE_CASE),
            "^WARN".toRegex(RegexOption.IGNORE_CASE),
            
            // Stack trace patterns
            "at \\w+\\.\\w+".toRegex(),
            "Exception".toRegex(),
            "\\w+Error".toRegex(),
            
            // File paths
            "/[/\\w]+/".toRegex(),
            "[A-Z]:\\\\[\\w\\\\]+".toRegex(), // Windows paths - FIXED escape sequence
        )
        
        // Check if text matches code patterns
        for (pattern in codePatterns) {
            if (pattern.containsMatchIn(filtered)) {
                Log.d(TAG, "Filtered code pattern: ${pattern.pattern} in '$filtered'")
                return "" // Don't speak code
            }
        }
        
        // Filter out common technical keywords
        val technicalKeywords = listOf(
            "null", "undefined", "NaN", "Infinity",
            "true", "false", "boolean",
            "int", "float", "double", "string",
            "void", "return", "class", "interface",
            "public", "private", "protected",
            "static", "final", "const",
            "var", "let", "val", "fun",
            "import", "export", "require",
            "findViewById", "onCreate", "onDestroy"
        )
        
        // Only filter if text is ONLY technical keywords
        val words = filtered.split("\\s+".toRegex())
        if (words.size <= 3 && words.all { it.lowercase() in technicalKeywords }) {
            Log.d(TAG, "Filtered technical keywords: $filtered")
            return ""
        }
        
        // Filter emojis and special symbols (but keep basic punctuation)
        filtered = filtered.replace("[\ud83c\udc00-\ud83e\uddff]".toRegex(), "")
        
        // Remove excessive special characters
        filtered = filtered.replace("[\u2705\u274c\u26a0\ufe0f\ud83d\udc4d\u270c\ufe0f\ud83d\udc4c\u270a\u270b\u261d\ufe0f]".toRegex(), "")
        
        // Clean up multiple spaces
        filtered = filtered.replace("\\s+".toRegex(), " ").trim()
        
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
     * ✅ FIXED: Set voice based on user preference - only discover selected gender
     */
    private fun setVoiceFromPreferences() {
        try {
            val selectedVoice = prefs.getString("selected_voice", "david") ?: "david"
            
            // ✅ FIXED: Only discover the voice that's actually selected
            val voice = when (selectedVoice.lowercase()) {
                "david", "male" -> {
                    currentVoiceGender = "male"
                    if (maleVoice == null) {
                        maleVoice = discoverVoice("male")
                    }
                    maleVoice
                }
                "dayana", "female" -> {
                    currentVoiceGender = "female"
                    if (femaleVoice == null) {
                        femaleVoice = discoverVoice("female")
                    }
                    femaleVoice
                }
                else -> tts?.voice
            }
            
            voice?.let {
                tts?.voice = it
                Log.d(TAG, "✅ Voice set to: ${it.name} (${currentVoiceGender})")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting voice", e)
        }
    }
    
    /**
     * ✅ FIXED: Change voice - clear unused voice cache
     */
    fun changeVoice(voiceId: String) {
        prefs.edit().putString("selected_voice", voiceId).apply()
        
        // Clear the opposite gender voice to save memory
        when (voiceId.lowercase()) {
            "david", "male" -> {
                femaleVoice = null
                currentVoiceGender = "male"
            }
            "dayana", "female" -> {
                maleVoice = null
                currentVoiceGender = "female"
            }
        }
        
        setVoiceFromPreferences()
        Log.d(TAG, "✅ Voice changed to: $voiceId")
    }
    
    /**
     * ✅ NEW: Get available voice options for UI
     */
    fun getAvailableVoices(): List<VoiceOption> {
        val voices = mutableListOf<VoiceOption>()
        
        // Discover both voices for UI display only (don't set them)
        val tempMale = if (maleVoice != null) maleVoice else discoverVoice("male")
        val tempFemale = if (femaleVoice != null) femaleVoice else discoverVoice("female")
        
        if (tempMale != null) {
            voices.add(VoiceOption(
                id = "david",
                name = "David (Male)",
                gender = "male",
                isAvailable = true
            ))
        } else {
            voices.add(VoiceOption(
                id = "david",
                name = "David (Male) - Not Available",
                gender = "male",
                isAvailable = false
            ))
        }
        
        if (tempFemale != null) {
            voices.add(VoiceOption(
                id = "dayana",
                name = "Dayana (Female)",
                gender = "female",
                isAvailable = true
            ))
        } else {
            voices.add(VoiceOption(
                id = "dayana",
                name = "Dayana (Female) - Not Available",
                gender = "female",
                isAvailable = false
            ))
        }
        
        return voices
    }
    
    /**
     * ✅ NEW: Get current voice selection
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
     * Cleanup (called automatically)
     */
    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
            maleVoice = null
            femaleVoice = null
            Log.d(TAG, "TTS engine shutdown")
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        }
    }
    
    /**
     * ✅ NEW: Voice option data class
     */
    data class VoiceOption(
        val id: String,
        val name: String,
        val gender: String,
        val isAvailable: Boolean
    )
    
    companion object {
        private const val TAG = "TextToSpeechEngine"
    }
}