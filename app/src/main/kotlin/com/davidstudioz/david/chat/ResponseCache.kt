package com.davidstudioz.david.chat

import android.util.Log

/**
 * ResponseCache - Prevents repetitive responses
 * ✅ Caches recent responses
 * ✅ Provides varied responses for similar questions
 * ✅ Expires old cache entries
 * ✅ Tracks conversation context
 */
class ResponseCache {
    
    private val cache = mutableMapOf<String, CachedResponse>()
    private val conversationHistory = mutableListOf<String>()
    private val maxHistorySize = 5
    private val cacheExpiryMs = 5 * 60 * 1000L // 5 minutes
    
    data class CachedResponse(
        val response: String,
        val timestamp: Long,
        val useCount: Int = 0
    )
    
    /**
     * Get cached response or null if not found/expired
     */
    fun get(question: String): String? {
        val normalized = normalizeQuestion(question)
        val cached = cache[normalized]
        
        if (cached != null) {
            val age = System.currentTimeMillis() - cached.timestamp
            if (age < cacheExpiryMs) {
                // Update use count
                cache[normalized] = cached.copy(useCount = cached.useCount + 1)
                Log.d(TAG, "Cache hit for: $question (used ${cached.useCount} times)")
                return if (cached.useCount > 2) {
                    // Vary response if used too many times
                    null
                } else {
                    cached.response
                }
            } else {
                // Expired - remove
                cache.remove(normalized)
            }
        }
        
        return null
    }
    
    /**
     * Store response in cache
     */
    fun put(question: String, response: String) {
        val normalized = normalizeQuestion(question)
        cache[normalized] = CachedResponse(
            response = response,
            timestamp = System.currentTimeMillis(),
            useCount = 0
        )
        
        // Add to conversation history
        conversationHistory.add("Q: $question\nA: $response")
        if (conversationHistory.size > maxHistorySize) {
            conversationHistory.removeAt(0)
        }
        
        Log.d(TAG, "Cached response for: $question")
    }
    
    /**
     * Check if question was recently asked
     */
    fun wasRecentlyAsked(question: String): Boolean {
        val normalized = normalizeQuestion(question)
        return cache.containsKey(normalized)
    }
    
    /**
     * Get conversation context
     */
    fun getContext(): String {
        return conversationHistory.takeLast(3).joinToString("\n\n")
    }
    
    /**
     * Clear cache
     */
    fun clear() {
        cache.clear()
        conversationHistory.clear()
        Log.d(TAG, "Cache cleared")
    }
    
    /**
     * Normalize question for matching
     */
    private fun normalizeQuestion(question: String): String {
        return question.lowercase()
            .replace("[^a-z0-9\\s]".toRegex(), "")
            .trim()
            .split("\\s+".toRegex())
            .sorted()
            .joinToString(" ")
    }
    
    companion object {
        private const val TAG = "ResponseCache"
    }
}