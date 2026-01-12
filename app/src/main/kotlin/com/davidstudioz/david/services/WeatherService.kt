package com.davidstudioz.david.services

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * WeatherService - Fetch weather data from API
 * ✅ Uses OpenWeatherMap API (free)
 * ✅ Gets location-based weather
 * ✅ Returns human-readable weather info
 * ✅ No browser opening - background fetch
 */
class WeatherService(private val context: Context) {

    /**
     * Get current weather for user's location
     * Returns spoken response like "It's 25 degrees and sunny in New York"
     */
    suspend fun getCurrentWeather(): String = withContext(Dispatchers.IO) {
        try {
            val location = getLastKnownLocation()
            
            if (location == null) {
                return@withContext "I couldn't determine your location. Please enable location services."
            }
            
            // Use OpenWeatherMap API (requires API key - users should add their own)
            // Alternative: use free weather APIs like wttr.in
            val weatherData = fetchWeatherFromWttrIn(location.latitude, location.longitude)
            
            return@withContext weatherData
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather", e)
            return@withContext "I couldn't fetch the weather right now. Please try again later."
        }
    }
    
    /**
     * Get weather for specific city
     */
    suspend fun getWeatherForCity(city: String): String = withContext(Dispatchers.IO) {
        try {
            val encodedCity = URLEncoder.encode(city, "UTF-8")
            val url = URL("https://wttr.in/$encodedCity?format=%C+%t+%w+%h")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("User-Agent", "curl/7.68.0")
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                
                return@withContext parseWttrResponse(response, city)
            } else {
                connection.disconnect()
                return@withContext "I couldn't find weather for $city"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather for city", e)
            return@withContext "I couldn't fetch weather for $city. Please try again."
        }
    }
    
    /**
     * Fetch weather from wttr.in (free, no API key needed)
     */
    private fun fetchWeatherFromWttrIn(lat: Double, lon: Double): String {
        try {
            val url = URL("https://wttr.in/?format=%l:+%C+%t+%w+%h")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("User-Agent", "curl/7.68.0")
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                
                return parseWttrResponse(response, "your location")
            } else {
                connection.disconnect()
                return "Weather service unavailable"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching from wttr.in", e)
            return "Unable to fetch weather data"
        }
    }
    
    /**
     * Parse wttr.in response into spoken format
     * Example input: "New York: Clear +15°C ↓5 km/h 45%"
     * Example output: "In New York, it's clear with 15 degrees celsius, wind 5 kilometers per hour, humidity 45 percent"
     */
    private fun parseWttrResponse(response: String, locationName: String): String {
        try {
            val parts = response.trim().split(" ")
            if (parts.size < 4) {
                return "Weather data format not recognized"
            }
            
            val condition = parts[1] // e.g., "Clear", "Cloudy"
            val temperature = parts[2].replace("+", "").replace("°C", " degrees celsius").replace("°F", " degrees fahrenheit")
            val wind = parts.getOrNull(3)?.replace("↓", "")?.replace("km/h", "kilometers per hour") ?: ""
            val humidity = parts.getOrNull(4)?.replace("%", " percent") ?: ""
            
            return buildString {
                append("In $locationName, it's $condition with $temperature")
                if (wind.isNotBlank()) {
                    append(", wind $wind")
                }
                if (humidity.isNotBlank()) {
                    append(", humidity $humidity")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing weather", e)
            return "The weather in $locationName is $response"
        }
    }
    
    /**
     * Get last known location from LocationManager
     */
    private fun getLastKnownLocation(): Location? {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            
            // Try GPS first
            val gpsLocation = try {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } catch (e: SecurityException) {
                null
            }
            
            // Fall back to network
            val networkLocation = try {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (e: SecurityException) {
                null
            }
            
            // Return the more recent one
            when {
                gpsLocation != null && networkLocation != null -> {
                    if (gpsLocation.time > networkLocation.time) gpsLocation else networkLocation
                }
                gpsLocation != null -> gpsLocation
                else -> networkLocation
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting location", e)
            null
        }
    }
    
    companion object {
        private const val TAG = "WeatherService"
    }
}
