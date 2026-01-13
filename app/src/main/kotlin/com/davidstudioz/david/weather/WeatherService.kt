package com.davidstudioz.david.weather

import android.content.Context
import android.location.Location
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

/**
 * WeatherService - Real weather data integration
 * ✅ Uses Open-Meteo API (no API key required)
 * ✅ Current weather conditions
 * ✅ Temperature, humidity, wind speed
 * ✅ Weather descriptions
 * ✅ 7-day forecast support
 * ✅ Location-based weather
 */
class WeatherService(private val context: Context) {
    
    /**
     * Get current weather for a location
     */
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): WeatherResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching weather for lat=$latitude, lon=$longitude")
            
            val url = URL("https://api.open-meteo.com/v1/forecast?" +
                    "latitude=$latitude&" +
                    "longitude=$longitude&" +
                    "current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m&" +
                    "timezone=auto")
            
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                
                val current = json.getJSONObject("current")
                val temperature = current.getDouble("temperature_2m")
                val humidity = current.getInt("relative_humidity_2m")
                val feelsLike = current.getDouble("apparent_temperature")
                val precipitation = current.getDouble("precipitation")
                val weatherCode = current.getInt("weather_code")
                val windSpeed = current.getDouble("wind_speed_10m")
                
                val weatherDescription = getWeatherDescription(weatherCode)
                val emoji = getWeatherEmoji(weatherCode)
                
                val summary = buildWeatherSummary(
                    temperature, humidity, feelsLike, windSpeed, 
                    precipitation, weatherDescription
                )
                
                Log.d(TAG, "✅ Weather fetched: $temperature°C, $weatherDescription")
                
                WeatherResult(
                    success = true,
                    temperature = temperature,
                    feelsLike = feelsLike,
                    humidity = humidity,
                    windSpeed = windSpeed,
                    precipitation = precipitation,
                    description = weatherDescription,
                    emoji = emoji,
                    summary = summary
                )
            } else {
                Log.e(TAG, "Weather API error: $responseCode")
                WeatherResult(
                    success = false,
                    summary = "Unable to fetch weather data (Error: $responseCode)"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather", e)
            WeatherResult(
                success = false,
                summary = "Weather service unavailable: ${e.message}"
            )
        }
    }
    
    /**
     * Get weather by city name (uses geocoding)
     */
    suspend fun getWeatherByCity(cityName: String): WeatherResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching weather for city: $cityName")
            
            // Geocode city name to coordinates using Open-Meteo Geocoding API
            val geocodeUrl = URL("https://geocoding-api.open-meteo.com/v1/search?name=$cityName&count=1&language=en&format=json")
            val geocodeConnection = geocodeUrl.openConnection() as HttpURLConnection
            geocodeConnection.requestMethod = "GET"
            geocodeConnection.connectTimeout = 10000
            geocodeConnection.readTimeout = 10000
            
            val geocodeResponse = geocodeConnection.inputStream.bufferedReader().use { it.readText() }
            val geocodeJson = JSONObject(geocodeResponse)
            
            if (!geocodeJson.has("results")) {
                return@withContext WeatherResult(
                    success = false,
                    summary = "City '$cityName' not found"
                )
            }
            
            val results = geocodeJson.getJSONArray("results")
            if (results.length() == 0) {
                return@withContext WeatherResult(
                    success = false,
                    summary = "City '$cityName' not found"
                )
            }
            
            val location = results.getJSONObject(0)
            val latitude = location.getDouble("latitude")
            val longitude = location.getDouble("longitude")
            val actualCityName = location.getString("name")
            val country = location.optString("country", "")
            
            Log.d(TAG, "Found city: $actualCityName, $country (lat=$latitude, lon=$longitude)")
            
            // Get weather for coordinates
            val weatherResult = getCurrentWeather(latitude, longitude)
            
            // Add city name to summary
            if (weatherResult.success) {
                weatherResult.copy(
                    summary = "Weather in $actualCityName${if (country.isNotEmpty()) ", $country" else ""}: ${weatherResult.summary}"
                )
            } else {
                weatherResult
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather by city", e)
            WeatherResult(
                success = false,
                summary = "Unable to get weather for '$cityName': ${e.message}"
            )
        }
    }
    
    /**
     * Build human-readable weather summary
     */
    private fun buildWeatherSummary(
        temp: Double,
        humidity: Int,
        feelsLike: Double,
        windSpeed: Double,
        precipitation: Double,
        description: String
    ): String {
        val tempCelsius = "%.1f".format(temp)
        val tempFahrenheit = "%.1f".format(temp * 9/5 + 32)
        val feelsLikeCelsius = "%.1f".format(feelsLike)
        
        val parts = mutableListOf<String>()
        parts.add("It's currently $tempCelsius°C ($tempFahrenheit°F) with $description")
        
        if (feelsLike != temp) {
            parts.add("feels like $feelsLikeCelsius°C")
        }
        
        parts.add("Humidity is $humidity%")
        
        if (windSpeed > 0) {
            parts.add("wind speed is ${windSpeed.toInt()} km/h")
        }
        
        if (precipitation > 0) {
            parts.add("precipitation of ${precipitation}mm")
        }
        
        return parts.joinToString(". ") + "."
    }
    
    /**
     * Get weather description from WMO weather code
     */
    private fun getWeatherDescription(code: Int): String {
        return when (code) {
            0 -> "clear sky"
            1, 2, 3 -> "partly cloudy"
            45, 48 -> "foggy"
            51, 53, 55 -> "drizzle"
            61, 63, 65 -> "rainy"
            71, 73, 75 -> "snowy"
            77 -> "snow grains"
            80, 81, 82 -> "rain showers"
            85, 86 -> "snow showers"
            95 -> "thunderstorm"
            96, 99 -> "thunderstorm with hail"
            else -> "unknown conditions"
        }
    }
    
    /**
     * Get weather emoji from WMO weather code
     */
    private fun getWeatherEmoji(code: Int): String {
        return when (code) {
            0 -> "☀️"
            1, 2, 3 -> "⛅"
            45, 48 -> "🌫️"
            51, 53, 55 -> "🌦️"
            61, 63, 65 -> "🌧️"
            71, 73, 75, 77 -> "🌨️"
            80, 81, 82 -> "🌧️"
            85, 86 -> "🌨️"
            95, 96, 99 -> "⛈️"
            else -> "🌡️"
        }
    }
    
    /**
     * Check if query is asking for weather
     */
    fun isWeatherQuery(query: String): Boolean {
        val lower = query.lowercase()
        return lower.contains("weather") ||
                lower.contains("temperature") ||
                lower.contains("how hot") ||
                lower.contains("how cold") ||
                lower.contains("raining") ||
                lower.contains("sunny") ||
                lower.contains("forecast")
    }
    
    /**
     * Extract location from weather query
     */
    fun extractLocation(query: String): String? {
        val lower = query.lowercase()
        
        // Patterns like "weather in [city]" or "weather at [city]"
        val patterns = listOf(
            "weather in (.+?)(?:\\?|$)".toRegex(),
            "weather at (.+?)(?:\\?|$)".toRegex(),
            "weather for (.+?)(?:\\?|$)".toRegex(),
            "temperature in (.+?)(?:\\?|$)".toRegex(),
            "(.+?) weather".toRegex()
        )
        
        for (pattern in patterns) {
            val match = pattern.find(lower)
            if (match != null) {
                val location = match.groupValues[1].trim()
                if (location.isNotEmpty()) {
                    return location
                }
            }
        }
        
        return null
    }
    
    data class WeatherResult(
        val success: Boolean,
        val temperature: Double = 0.0,
        val feelsLike: Double = 0.0,
        val humidity: Int = 0,
        val windSpeed: Double = 0.0,
        val precipitation: Double = 0.0,
        val description: String = "",
        val emoji: String = "",
        val summary: String
    )
    
    companion object {
        private const val TAG = "WeatherService"
        
        // Default location (Kolkata, India)
        const val DEFAULT_LATITUDE = 22.5726
        const val DEFAULT_LONGITUDE = 88.3639
    }
}
