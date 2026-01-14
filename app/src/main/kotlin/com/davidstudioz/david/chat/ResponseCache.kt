package com.davidstudioz.david.chat

import android.util.LruCache

/**
 * ResponseCache - Caches chat responses for faster replies
 * ✅ LRU cache (least recently used eviction)
 * ✅ 24-hour expiry for cached responses
 * ✅ Memory efficient (100 responses max)
 */
class ResponseCache {
    
    private data class CachedResponse(
        val response: String,
        val timestamp: Long
    )
    
    // LRU cache with max 100 entries
    private val cache = LruCache<String, CachedResponse>(100)
    
    // Cache expiry time (24 hours)
    private val cacheExpiryMs = 24 * 60 * 60 * 1000L
    
    /**
     * Get cached response if available and not expired
     */
    fun get(query: String): String? {
        val normalizedQuery = normalizeQuery(query)
        val cached = cache.get(normalizedQuery)
        
        if (cached != null) {
            val age = System.currentTimeMillis() - cached.timestamp
            if (age < cacheExpiryMs) {
                return cached.response
            } else {
                // Expired, remove from cache
                cache.remove(normalizedQuery)
            }
        }
        
        return null
    }
    
    /**
     * Store response in cache
     */
    fun put(query: String, response: String) {
        val normalizedQuery = normalizeQuery(query)
        cache.put(
            normalizedQuery,
            CachedResponse(
                response = response,
                timestamp = System.currentTimeMillis()
            )
        )
    }
    
    /**
     * Clear all cached responses
     */
    fun clear() {
        cache.evictAll()
    }
    
    /**
     * Normalize query for consistent caching
     */
    private fun normalizeQuery(query: String): String {
        return query.lowercase()
            .trim()
            .replace("\\s+".toRegex(), " ")
            .take(200) // Limit key length
    }
    
    /**
     * Get cache statistics
     */
    fun getStats(): Map<String, Any> {
        return mapOf(
            "size" to cache.size(),
            "maxSize" to cache.maxSize(),
            "hitCount" to cache.hitCount(),
            "missCount" to cache.missCount(),
            "hitRate" to if (cache.hitCount() + cache.missCount() > 0) {
                cache.hitCount().toFloat() / (cache.hitCount() + cache.missCount())
            } else 0f
        )
    }
}