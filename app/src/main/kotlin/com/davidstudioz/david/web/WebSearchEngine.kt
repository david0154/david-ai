package com.davidstudioz.david.web

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * WebSearchEngine - Search the web and fetch pages
 * ✅ Uses DuckDuckGo Instant Answer API (free, no key)
 * ✅ Wikipedia integration for detailed info
 * ✅ Web page fetching and parsing
 * ✅ Returns SearchResult objects
 * ✅ No OkHttp dependency - uses HttpURLConnection
 * Connected to: SearchService, VoiceController
 */
class WebSearchEngine {
    
    data class SearchResult(
        val title: String,
        val url: String,
        val snippet: String
    )
    
    /**
     * Search the web using DuckDuckGo Instant Answer API
     * Returns list of SearchResult objects
     */
    suspend fun search(query: String, maxResults: Int = 5): Result<List<SearchResult>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val results = mutableListOf<SearchResult>()
            
            // Search DuckDuckGo
            val ddgResults = searchDuckDuckGo(query)
            results.addAll(ddgResults)
            
            // If no results, try Wikipedia
            if (results.isEmpty()) {
                val wikiResults = searchWikipedia(query)
                results.addAll(wikiResults)
            }
            
            // Limit to maxResults
            val limitedResults = results.take(maxResults)
            
            Log.d(TAG, "Found ${limitedResults.size} results for: $query")
            Result.success(limitedResults)
        } catch (e: Exception) {
            Log.e(TAG, "Search error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Search DuckDuckGo Instant Answer API
     */
    private fun searchDuckDuckGo(query: String): List<SearchResult> {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://api.duckduckgo.com/?q=$encodedQuery&format=json&no_html=1")
            
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("User-Agent", "DAVID-AI/1.0")
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                
                return parseDuckDuckGoResults(response, query)
            } else {
                connection.disconnect()
                return emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "DuckDuckGo search error", e)
            return emptyList()
        }
    }
    
    /**
     * Parse DuckDuckGo JSON response into SearchResult objects
     */
    private fun parseDuckDuckGoResults(jsonResponse: String, query: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        
        try {
            val json = JSONObject(jsonResponse)
            
            // Check for instant answer
            val answer = json.optString("Answer")
            if (answer.isNotBlank()) {
                results.add(
                    SearchResult(
                        title = "Instant Answer: $query",
                        url = json.optString("AbstractURL", "https://duckduckgo.com/?q=${URLEncoder.encode(query, "UTF-8")}"),
                        snippet = answer
                    )
                )
            }
            
            // Check for abstract
            val abstract = json.optString("Abstract")
            if (abstract.isNotBlank()) {
                results.add(
                    SearchResult(
                        title = json.optString("Heading", query),
                        url = json.optString("AbstractURL", "https://duckduckgo.com/?q=${URLEncoder.encode(query, "UTF-8")}"),
                        snippet = abstract
                    )
                )
            }
            
            // Check for definition
            val definition = json.optString("Definition")
            if (definition.isNotBlank()) {
                results.add(
                    SearchResult(
                        title = "Definition: $query",
                        url = json.optString("DefinitionURL", "https://duckduckgo.com/?q=${URLEncoder.encode(query, "UTF-8")}"),
                        snippet = definition
                    )
                )
            }
            
            // Check for related topics
            val relatedTopics = json.optJSONArray("RelatedTopics")
            if (relatedTopics != null) {
                for (i in 0 until minOf(relatedTopics.length(), 3)) {
                    val topic = relatedTopics.getJSONObject(i)
                    val text = topic.optString("Text")
                    val firstURL = topic.optString("FirstURL")
                    
                    if (text.isNotBlank() && firstURL.isNotBlank()) {
                        // Extract title from text (usually first part before dash)
                        val title = text.split(" - ").firstOrNull() ?: text.take(50)
                        results.add(
                            SearchResult(
                                title = title,
                                url = firstURL,
                                snippet = text
                            )
                        )
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing DuckDuckGo results", e)
        }
        
        return results
    }
    
    /**
     * Search Wikipedia for query
     */
    private fun searchWikipedia(query: String): List<SearchResult> {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=$encodedQuery&format=json&srlimit=3")
            
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("User-Agent", "DAVID-AI/1.0")
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                
                return parseWikipediaResults(response)
            } else {
                connection.disconnect()
                return emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Wikipedia search error", e)
            return emptyList()
        }
    }
    
    /**
     * Parse Wikipedia JSON response
     */
    private fun parseWikipediaResults(jsonResponse: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        
        try {
            val json = JSONObject(jsonResponse)
            val query = json.getJSONObject("query")
            val searchResults = query.getJSONArray("search")
            
            for (i in 0 until searchResults.length()) {
                val result = searchResults.getJSONObject(i)
                val title = result.getString("title")
                val snippet = result.getString("snippet")
                    .replace("<span class=\"searchmatch\">", "")
                    .replace("</span>", "")
                    .replace("&quot;", "\"")
                
                val pageId = result.getInt("pageid")
                val url = "https://en.wikipedia.org/?curid=$pageId"
                
                results.add(
                    SearchResult(
                        title = title,
                        url = url,
                        snippet = snippet
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Wikipedia results", e)
        }
        
        return results
    }
    
    /**
     * Fetch and parse a web page
     * Returns the text content of the page
     */
    suspend fun fetchWebPage(urlString: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val html = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                
                // Basic HTML parsing (remove tags)
                val text = html
                    .replace(Regex("<script[^>]*>.*?</script>", RegexOption.DOT_MATCHES_ALL), "")
                    .replace(Regex("<style[^>]*>.*?</style>", RegexOption.DOT_MATCHES_ALL), "")
                    .replace(Regex("<[^>]+>"), " ")
                    .replace(Regex("\\s+"), " ")
                    .trim()
                
                Log.d(TAG, "Fetched page: ${text.take(100)}...")
                Result.success(text)
            } else {
                connection.disconnect()
                Result.failure(Exception("HTTP error: $responseCode"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching web page", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get a summary from search results (for voice response)
     */
    fun getSummaryFromResults(results: List<SearchResult>): String {
        if (results.isEmpty()) {
            return "No results found"
        }
        
        val firstResult = results.first()
        val snippet = firstResult.snippet
        
        // Limit to first 2 sentences for spoken response
        val sentences = snippet.split(". ")
        val summary = sentences.take(2).joinToString(". ")
        
        return "${firstResult.title}. $summary"
    }
    
    companion object {
        private const val TAG = "WebSearchEngine"
    }
}
