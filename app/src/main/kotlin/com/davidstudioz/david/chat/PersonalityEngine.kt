package com.davidstudioz.david.chat

import kotlin.random.Random

/**
 * PersonalityEngine - Makes D.A.V.I.D feel human
 * ✅ Adds personality traits
 * ✅ Varies responses naturally
 * ✅ Adds conversational fillers
 * ✅ Shows empathy and humor
 */
class PersonalityEngine {
    
    private val acknowledgments = listOf(
        "I see", "Got it", "I understand", "Okay", "Alright",
        "Sure", "Absolutely", "Of course", "Right", "Makes sense"
    )
    
    private val transitions = listOf(
        "Let me help with that", "Here's what I found",
        "I can assist you with this", "Let me explain",
        "Allow me to help", "I've got this", "No problem"
    )
    
    private val positiveReactions = listOf(
        "Great question!", "Interesting!", "Good point!",
        "That's a smart question!", "Excellent!"
    )
    
    private val uncertaintyPhrases = listOf(
        "I'm not entirely sure, but", "From what I know",
        "As far as I understand", "I believe", "It seems like"
    )
    
    /**
     * Add personality to response
     */
    fun personalize(response: String, confidence: Float = 0.8f): String {
        val parts = mutableListOf<String>()
        
        // Add occasional acknowledgment (30% chance)
        if (Random.nextFloat() < 0.3f) {
            parts.add(acknowledgments.random())
        }
        
        // Add transition for longer responses
        if (response.length > 50 && Random.nextFloat() < 0.4f) {
            parts.add(transitions.random())
        }
        
        // Add uncertainty for low confidence
        if (confidence < 0.6f && Random.nextFloat() < 0.5f) {
            parts.add(uncertaintyPhrases.random())
        }
        
        // Add main response
        parts.add(response)
        
        return parts.joinToString(". ").trim()
    }
    
    /**
     * Vary similar responses
     */
    fun vary(baseResponse: String, variation: Int): String {
        return when (variation % 5) {
            0 -> baseResponse
            1 -> "Sure! $baseResponse"
            2 -> "Of course. $baseResponse"
            3 -> "No problem. $baseResponse"
            4 -> "Happy to help! $baseResponse"
            else -> baseResponse
        }
    }
    
    /**
     * Add empathy to response
     */
    fun addEmpathy(response: String, userEmotion: String): String {
        val empathyPrefix = when (userEmotion.lowercase()) {
            "frustrated", "angry" -> "I understand this can be frustrating. "
            "confused" -> "No worries, let me clarify. "
            "happy", "excited" -> "That's wonderful! "
            "sad", "disappointed" -> "I'm sorry to hear that. "
            else -> ""
        }
        
        return empathyPrefix + response
    }
    
    /**
     * Make response more conversational
     */
    fun makeConversational(response: String): String {
        var conversational = response
        
        // Add conversational punctuation
        if (!conversational.endsWith(".") && !conversational.endsWith("!") && !conversational.endsWith("?")) {
            conversational += "."
        }
        
        // Break long sentences
        if (conversational.length > 150) {
            conversational = conversational.replace(". ", ".\n").replace(", and ", ".\n")
        }
        
        return conversational
    }
    
    /**
     * Get random positive reaction
     */
    fun getPositiveReaction(): String = positiveReactions.random()
    
    /**
     * Get random acknowledgment
     */
    fun getAcknowledgment(): String = acknowledgments.random()
}