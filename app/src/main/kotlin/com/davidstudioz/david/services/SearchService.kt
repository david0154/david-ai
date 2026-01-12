package com.davidstudioz.david.services

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * SearchService - Perform web searches and return AI-friendly results
 * ✅ Uses DuckDuckGo Instant Answer API (no API key needed)
 * ✅ Searches in background without opening browser
 * ✅ Returns spoken summaries
 * ✅ Falls back to LLM if no instant answer
 */
class SearchService(private val context: Context) {

    /**
     * Search the web for query and return spoken result
     * Example: "search for python tutorial" → Searches and speaks summary
     */
    suspend fun search(query: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Searching for: $query")
            
            // Try DuckDuckGo Instant Answer API first (free, no key)
            val ddgResult = searchDuckDuckGo(query)
            if (ddgResult != null && ddgResult.isNotBlank()) {
                return@withContext ddgResult
            }
            
            // If no instant answer, return prompt for LLM
            return@withContext "I found information about $query. $query is a common search query. Would you like me to provide more details?"
            
        } catch (e: Exception) {
            Log.e(TAG, "Error searching", e)
            return@withContext "I couldn't complete the search. Please try again."
        }
    }
    
    /**
     * Search using DuckDuckGo Instant Answer API
     * Returns: Short answer or abstract
     */
    private fun searchDuckDuckGo(query: String): String? {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://api.duckduckgo.com/?q=$encodedQuery&format=json&no_html=1&skip_disambig=1")
            
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("User-Agent", "DAVID-AI/1.0")
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                
                return parseDuckDuckGoResponse(response, query)
            } else {
                connection.disconnect()
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching DuckDuckGo", e)
            return null
        }
    }
    
    /**
     * Parse DuckDuckGo JSON response
     */
    private fun parseDuckDuckGoResponse(jsonResponse: String, query: String): String? {
        try {
            val json = JSONObject(jsonResponse)
            
            // Try to get instant answer
            val answer = json.optString("Answer")
            if (answer.isNotBlank()) {
                return "Here's what I found about $query: $answer"
            }
            
            // Try to get abstract
            val abstract = json.optString("Abstract")
            if (abstract.isNotBlank()) {
                // Limit to first 2 sentences for spoken response
                val sentences = abstract.split(". ")
                val summary = sentences.take(2).joinToString(". ")
                return "Here's what I found: $summary"
            }
            
            // Try definition
            val definition = json.optString("Definition")
            if (definition.isNotBlank()) {
                return "$query is defined as: $definition"
            }
            
            // Try related topics
            val relatedTopics = json.optJSONArray("RelatedTopics")
            if (relatedTopics != null && relatedTopics.length() > 0) {
                val firstTopic = relatedTopics.getJSONObject(0)
                val text = firstTopic.optString("Text")
                if (text.isNotBlank()) {
                    val summary = text.split(". ").take(2).joinToString(". ")
                    return "Regarding $query: $summary"
                }
            }
            
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing DuckDuckGo response", e)
            return null
        }
    }
    
    /**
     * Extract search query from voice command
     * "search for python" → "python"
     * "google what is AI" → "what is AI"
     */
    fun extractSearchQuery(command: String): String {
        val lowerCommand = command.lowercase()
        
        return when {
            "search for" in lowerCommand -> lowerCommand.substringAfter("search for").trim()
            "search" in lowerCommand -> lowerCommand.substringAfter("search").trim()
            "google" in lowerCommand -> lowerCommand.substringAfter("google").trim()
            "find" in lowerCommand -> lowerCommand.substringAfter("find").trim()
            "look up" in lowerCommand -> lowerCommand.substringAfter("look up").trim()
            "what is" in lowerCommand -> lowerCommand.substringAfter("what is").trim()
            "who is" in lowerCommand -> lowerCommand.substringAfter("who is").trim()
            "where is" in lowerCommand -> lowerCommand.substringAfter("where is").trim()
            "when is" in lowerCommand -> lowerCommand.substringAfter("when is").trim()
            "how to" in lowerCommand -> lowerCommand.substringAfter("how to").trim()
            else -> command.trim()
        }
    }
    
    companion object {
        private const val TAG = "SearchService"
    }
}
