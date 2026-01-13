package com.davidstudioz.david.web

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URLEncoder

/**
 * WebSearchEngine - Internet search integration for D.A.V.I.D
 * ✅ Real-time web searches
 * ✅ Uses DuckDuckGo HTML (no API key needed)
 * ✅ Extracts search results
 * ✅ Internet connectivity check
 * ✅ Fallback handling
 */
class WebSearchEngine(private val context: Context) {
    
    /**
     * Search the web for a query
     */
    suspend fun search(query: String): SearchResult = withContext(Dispatchers.IO) {
        try {
            if (!isInternetAvailable()) {
                return@withContext SearchResult(
                    success = false,
                    query = query,
                    summary = "No internet connection available",
                    sources = emptyList()
                )
            }
            
            Log.d(TAG, "Searching web for: $query")
            
            // Use DuckDuckGo HTML search (no API key required)
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val searchUrl = "https://html.duckduckgo.com/html/?q=$encodedQuery"
            
            val doc = Jsoup.connect(searchUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get()
            
            // Extract search results
            val results = mutableListOf<SearchSource>()
            val resultElements = doc.select(".result")
            
            resultElements.take(5).forEach { element ->
                try {
                    val titleElement = element.select(".result__title a")
                    val snippetElement = element.select(".result__snippet")
                    val urlElement = element.select(".result__url")
                    
                    val title = titleElement.text()
                    val snippet = snippetElement.text()
                    val url = urlElement.attr("href")
                    
                    if (title.isNotBlank() && snippet.isNotBlank()) {
                        results.add(
                            SearchSource(
                                title = title,
                                snippet = snippet,
                                url = url
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing result: ${e.message}")
                }
            }
            
            if (results.isEmpty()) {
                return@withContext SearchResult(
                    success = false,
                    query = query,
                    summary = "No results found for '$query'",
                    sources = emptyList()
                )
            }
            
            // Create summary from top results
            val summary = buildSummary(query, results)
            
            Log.d(TAG, "Found ${results.size} results for '$query'")
            
            SearchResult(
                success = true,
                query = query,
                summary = summary,
                sources = results
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Web search error", e)
            SearchResult(
                success = false,
                query = query,
                summary = "Search failed: ${e.message}",
                sources = emptyList()
            )
        }
    }
    
    /**
     * Build a summary from search results
     */
    private fun buildSummary(query: String, results: List<SearchSource>): String {
        if (results.isEmpty()) return "No information found."
        
        val topResult = results.first()
        return "Based on web search: ${topResult.snippet}"
    }
    
    /**
     * Check if internet is available
     */
    private fun isInternetAvailable(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking internet", e)
            false
        }
    }
    
    /**
     * Check if query needs web search
     */
    fun needsWebSearch(query: String): Boolean {
        val lower = query.lowercase()
        
        return lower.contains("search for") ||
                lower.contains("google") ||
                lower.contains("look up") ||
                lower.contains("find information") ||
                lower.contains("latest news") ||
                lower.contains("current price") ||
                lower.contains("today's") ||
                lower.contains("what happened") ||
                lower.contains("recent") ||
                lower.contains("new") && lower.contains("2026")
    }
    
    data class SearchResult(
        val success: Boolean,
        val query: String,
        val summary: String,
        val sources: List<SearchSource>
    )
    
    data class SearchSource(
        val title: String,
        val snippet: String,
        val url: String
    )
    
    companion object {
        private const val TAG = "WebSearchEngine"
    }
}