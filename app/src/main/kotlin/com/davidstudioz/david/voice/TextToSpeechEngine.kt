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
 * ✅ FIXED: Enhanced filtering to prevent speaking debug/internal code
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
     * ✅ FIXED: Speak text with enhanced filtering
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
     * ✅ ENHANCED: Filter internal code and technical strings with stricter rules
     */
    private fun filterInternalCode(text: String): String {
        var filtered = text.trim()
        
        // Return empty if text is too short (likely debug output)
        if (filtered.length < 2) {
            return ""
        }
        
        // ✅ NEW: Check for common debug prefixes and patterns first
        val debugPrefixes = listOf(
            "bilon", "debug", "error", "warn", "info", "log:", "tag:",
            "exception", "stacktrace", "null", "undefined",
            "system", "internal", "code:", "status:",
            "initialized", "loading", "processing"
        )
        
        val lowerFiltered = filtered.lowercase()
        for (prefix in debugPrefixes) {
            if (lowerFiltered.startsWith(prefix) || lowerFiltered.contains(" $prefix")) {
                Log.d(TAG, "Filtered debug prefix: $prefix in '$filtered'")
                return ""
            }
        }
        
        // Remove if it looks like code or technical output
        val codePatterns = listOf(
            // Programming patterns
            "\\w+\\.\\w+\\.\\w+".toRegex(), // Package names (com.example.app)
            "\\w+::\\w+".toRegex(), // Method references (Class::method)
            "\\w+\\(\\)".toRegex(), // Function calls with empty params
            "\\{.*\\}".toRegex(), // JSON/code blocks
            "\\[.*\\]".toRegex(), // Arrays/lists (but allow single brackets)
            "<.*>".toRegex(), // XML/HTML tags
            
            // Debug/log patterns
            "^\\[\\w+\\]".toRegex(), // [TAG] patterns
            "^\\w+:\\s*".toRegex(), // TAG: patterns at start
            "^DEBUG".toRegex(RegexOption.IGNORE_CASE),
            "^ERROR".toRegex(RegexOption.IGNORE_CASE),
            "^INFO".toRegex(RegexOption.IGNORE_CASE),
            "^WARN".toRegex(RegexOption.IGNORE_CASE),
            "^LOG".toRegex(RegexOption.IGNORE_CASE),
            
            // Stack trace patterns
            "at \\w+\\.\\w+".toRegex(),
            "Exception".toRegex(),
            "\\w+Error".toRegex(),
            "Throwable".toRegex(),
            
            // File paths
            "/[/\\w]+/".toRegex(),
            "[A-Z]:\\\\[\\w\\\\]+".toRegex(), // Windows paths
            
            // ✅ NEW: Hex, memory addresses, IDs
            "0x[0-9a-fA-F]+".toRegex(), // Hex addresses
            "[0-9a-f]{8}-[0-9a-f]{4}".toRegex(), // UUID patterns
        )
        
        // Check if text matches code patterns
        for (pattern in codePatterns) {
            if (pattern.containsMatchIn(filtered)) {
                Log.d(TAG, "Filtered code pattern: ${pattern.pattern} in '$filtered'")
                return "" // Don't speak code
            }
        }
        
        // ✅ NEW: Filter words that are purely technical/internal
        val technicalWords = setOf(
            "null", "undefined", "nan", "infinity",
            "true", "false", "boolean",
            "int", "float", "double", "string", "char", "byte",
            "void", "return", "class", "interface", "enum",
            "public", "private", "protected", "internal",
            "static", "final", "const", "val", "var",
            "fun", "function", "method",
            "import", "export", "require", "include",
            "package", "namespace",
            "findviewbyid", "oncreate", "ondestroy", "onresume",
            "kotlin", "java", "android",
            "bilon", // Specifically filter this
            "initialized", "loading", "processing"
        )
        
        // Count technical words
        val words = filtered.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val technicalCount = words.count { it.lowercase() in technicalWords }
        
        // If more than 50% of words are technical, don't speak
        if (words.isNotEmpty() && technicalCount.toFloat() / words.size > 0.5f) {
            Log.d(TAG, "Filtered technical content: $technicalCount/${words.size} technical words in '$filtered'")
            return ""
        }
        
        // If text is only 1-3 words and ANY are technical, filter it
        if (words.size <= 3 && words.any { it.lowercase() in technicalWords }) {
            Log.d(TAG, "Filtered short technical phrase: '$filtered'")
            return ""
        }
        
        // ✅ NEW: Filter if text looks like a variable name or identifier
        if (filtered.matches("[a-z]+[A-Z][a-zA-Z]*".toRegex())) { // camelCase
            Log.d(TAG, "Filtered camelCase identifier: '$filtered'")
            return ""
        }
        if (filtered.matches("[A-Z_][A-Z0-9_]+".toRegex())) { // UPPER_SNAKE_CASE
            Log.d(TAG, "Filtered constant: '$filtered'")
            return ""
        }
        
        // Filter emojis and special symbols (but keep basic punctuation)
        filtered = filtered.replace("[\ud83c\udc00-\ud83e\uddff]".toRegex(), "")
        
        // Remove excessive special characters
        filtered = filtered.replace("[\u2705\u274c\u26a0\ufe0f\ud83d\udc4d\u270c\ufe0f\ud83d\udc4c\u270a\u270b\u261d\ufe0f]".toRegex(), "")
        
        // Clean up multiple spaces
        filtered = filtered.replace("\\s+".toRegex(), " ").trim()
        
        // ✅ NEW: Final check - if result is too short after filtering, don't speak
        if (filtered.length < 3) {
            Log.d(TAG, "Filtered - too short after cleanup: '$filtered'")
            return ""
        }
        
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