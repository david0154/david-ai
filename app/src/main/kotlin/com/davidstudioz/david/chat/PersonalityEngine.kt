package com.davidstudioz.david.chat

import kotlin.random.Random

/**
 * PersonalityEngine - Adds personality and natural variation to responses
 * ✅ Multiple personality modes (friendly, professional, enthusiastic)
 * ✅ Response variations to avoid repetitiveness
 * ✅ Emoji support for warmth
 * ✅ Nexuzy Tech branding preserved
 */
class PersonalityEngine {
    
    // Current personality mode
    private var currentMode = PersonalityMode.FRIENDLY
    
    // Personality modes
    enum class PersonalityMode {
        FRIENDLY,      // Warm, casual, emoji-rich
        PROFESSIONAL,  // Clear, concise, helpful
        ENTHUSIASTIC   // Excited, energetic, motivating
    }
    
    /**
     * Add personality to a response
     */
    fun personalize(response: String): String {
        // Don't modify already personalized responses
        if (isAlreadyPersonalized(response)) {
            return response
        }
        
        return when (currentMode) {
            PersonalityMode.FRIENDLY -> addFriendlyTouch(response)
            PersonalityMode.PROFESSIONAL -> addProfessionalTouch(response)
            PersonalityMode.ENTHUSIASTIC -> addEnthusiasticTouch(response)
        }
    }
    
    /**
     * Check if response is already personalized
     */
    private fun isAlreadyPersonalized(response: String): Boolean {
        // Check for emojis, exclamations, or greeting patterns
        val emojiPattern = "[😀-🙏💀-🙏]".toRegex()
        val hasEmoji = emojiPattern.containsMatchIn(response)
        val hasGreeting = response.contains("Hello!", ignoreCase = true) ||
                         response.contains("Hi there!", ignoreCase = true)
        val hasExclamation = response.count { it == '!' } >= 2
        
        return hasEmoji || hasGreeting || hasExclamation
    }
    
    /**
     * Add friendly personality
     */
    private fun addFriendlyTouch(response: String): String {
        var result = response
        
        // Add occasional emojis (20% chance)
        if (Random.nextFloat() < 0.2) {
            val friendlyEmojis = listOf("😊", "👍", "✨", "🎯", "💡")
            result = "${friendlyEmojis.random()} $result"
        }
        
        // Add friendly closings (10% chance)
        if (Random.nextFloat() < 0.1 && !result.endsWith("!")) {
            val closings = listOf(
                "Hope this helps!",
                "Let me know if you need anything else!",
                "Feel free to ask more questions!"
            )
            result = "$result ${closings.random()}"
        }
        
        return result
    }
    
    /**
     * Add professional personality
     */
    private fun addProfessionalTouch(response: String): String {
        // Professional mode keeps responses clean and direct
        return response
    }
    
    /**
     * Add enthusiastic personality
     */
    private fun addEnthusiasticTouch(response: String): String {
        var result = response
        
        // Add enthusiasm (30% chance)
        if (Random.nextFloat() < 0.3) {
            val enthusiasticPrefixes = listOf(
                "Great question!",
                "Awesome!",
                "Fantastic!",
                "Excellent!"
            )
            result = "${enthusiasticPrefixes.random()} $result"
        }
        
        // Add energetic emojis (25% chance)
        if (Random.nextFloat() < 0.25) {
            val energeticEmojis = listOf("🚀", "⚡", "🔥", "💪", "🎉")
            result = "$result ${energeticEmojis.random()}"
        }
        
        return result
    }
    
    /**
     * Set personality mode
     */
    fun setMode(mode: PersonalityMode) {
        currentMode = mode
    }
    
    /**
     * Get current mode
     */
    fun getMode(): PersonalityMode = currentMode
    
    /**
     * Generate context-aware response variations
     */
    fun varyResponse(baseResponse: String, context: String): String {
        // Add contextual variations based on conversation flow
        return when {
            context.contains("repeat", ignoreCase = true) -> {
                "Let me rephrase: $baseResponse"
            }
            context.contains("explain", ignoreCase = true) -> {
                "To clarify: $baseResponse"
            }
            context.contains("more", ignoreCase = true) -> {
                "Additionally: $baseResponse"
            }
            else -> baseResponse
        }
    }
    
    /**
     * Add Nexuzy Tech branding to specific responses
     */
    fun addBranding(response: String, includeLink: Boolean = false): String {
        return if (includeLink) {
            "$response\n\n🌐 Powered by Nexuzy Tech - Visit nexuzy.tech"
        } else {
            "$response\n\n✨ By Nexuzy Tech"
        }
    }
}