package com.davidstudioz.david.weather

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

/**
 * WeatherService - Real weather data integration
 * ✅ Uses Open-Meteo API (no API key required)
 * ✅ Gets actual weather data, not web URLs
 * ✅ Location-based weather
 * ✅ Current conditions, forecast, temperature
 * ✅ Weather descriptions in natural language
 */
class WeatherService(private val context: Context) {

    private val prefs = context.getSharedPreferences("david_weather", Context.MODE_PRIVATE)
    
    /**
     * Get current weather for a location
     */
    suspend fun getCurrentWeather(location: String? = null): WeatherResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting weather for: ${location ?: "current location"}")
            
            // Get coordinates
            val coords = if (location != null) {
                getCoordinatesFromLocation(location)
            } else {
                getLastKnownCoordinates()
            }
            
            if (coords == null) {
                return@withContext WeatherResult(
                    success = false,
                    message = "Unable to determine location. Try asking 'weather in [city name]'"
                )
            }
            
            // Fetch weather from Open-Meteo API
            val weatherData = fetchWeatherData(coords.first, coords.second)
            
            if (weatherData != null) {
                val cityName = location ?: getCityName(coords.first, coords.second)
                val description = formatWeatherDescription(weatherData, cityName)
                
                WeatherResult(
                    success = true,
                    message = description,
                    temperature = weatherData.temperature,
                    condition = weatherData.condition,
                    humidity = weatherData.humidity,
                    windSpeed = weatherData.windSpeed,
                    location = cityName
                )
            } else {
                WeatherResult(
                    success = false,
                    message = "Unable to fetch weather data. Please check your internet connection."
                )
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Weather fetch error", e)
            WeatherResult(
                success = false,
                message = "Weather service temporarily unavailable. Try again later."
            )
        }
    }
    
    /**
     * Fetch weather data from Open-Meteo API
     */
    private suspend fun fetchWeatherData(latitude: Double, longitude: Double): WeatherData? = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=$latitude&longitude=$longitude" +
                    "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m" +
                    "&temperature_unit=celsius&wind_speed_unit=kmh"
            
            Log.d(TAG, "Fetching weather from: $url")
            
            val response = URL(url).readText()
            val json = JSONObject(response)
            val current = json.getJSONObject("current")
            
            val temp = current.getDouble("temperature_2m")
            val humidity = current.getInt("relative_humidity_2m")
            val windSpeed = current.getDouble("wind_speed_10m")
            val weatherCode = current.getInt("weather_code")
            
            val condition = getWeatherCondition(weatherCode)
            
            WeatherData(
                temperature = temp,
                condition = condition,
                humidity = humidity,
                windSpeed = windSpeed,
                weatherCode = weatherCode
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather data", e)
            null
        }
    }
    
    /**
     * Convert weather code to condition description
     * WMO Weather interpretation codes
     */
    private fun getWeatherCondition(code: Int): String {
        return when (code) {
            0 -> "Clear sky"
            1, 2, 3 -> "Partly cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing drizzle"
            61, 63, 65 -> "Rain"
            66, 67 -> "Freezing rain"
            71, 73, 75 -> "Snow"
            77 -> "Snow grains"
            80, 81, 82 -> "Rain showers"
            85, 86 -> "Snow showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with hail"
            else -> "Unknown"
        }
    }
    
    /**
     * Format weather into natural language
     */
    private fun formatWeatherDescription(data: WeatherData, location: String): String {
        val tempCelsius = data.temperature.toInt()
        val tempFahrenheit = (data.temperature * 9/5 + 32).toInt()
        
        val tempFeeling = when {
            tempCelsius < 0 -> "freezing cold"
            tempCelsius < 10 -> "cold"
            tempCelsius < 20 -> "cool"
            tempCelsius < 28 -> "pleasant"
            tempCelsius < 35 -> "warm"
            else -> "very hot"
        }
        
        return buildString {
            append("The weather in $location is currently ${data.condition.lowercase()} ")
            append("with a temperature of ${tempCelsius}°C (${tempFahrenheit}°F), ")
            append("which feels $tempFeeling. ")
            append("Humidity is at ${data.humidity}% ")
            append("and wind speed is ${data.windSpeed.toInt()} km/h.")
            
            // Add contextual advice
            when {
                data.condition.contains("rain", ignoreCase = true) -> 
                    append(" Don't forget your umbrella!")
                data.condition.contains("snow", ignoreCase = true) -> 
                    append(" Drive carefully in the snow!")
                tempCelsius > 35 -> 
                    append(" Stay hydrated in this heat!")
                tempCelsius < 5 -> 
                    append(" Bundle up, it's cold outside!")
            }
        }
    }
    
    /**
     * Get coordinates from location name
     */
    private suspend fun getCoordinatesFromLocation(location: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(location, 1)
            
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val lat = address.latitude
                val lon = address.longitude
                
                // Save for future use
                prefs.edit()
                    .putString("last_location", location)
                    .putFloat("last_latitude", lat.toFloat())
                    .putFloat("last_longitude", lon.toFloat())
                    .apply()
                
                Log.d(TAG, "Coordinates for $location: ($lat, $lon)")
                return@withContext Pair(lat, lon)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting coordinates", e)
        }
        return@withContext null
    }
    
    /**
     * Get last known coordinates (fallback)
     */
    private fun getLastKnownCoordinates(): Pair<Double, Double>? {
        val lat = prefs.getFloat("last_latitude", 0f).toDouble()
        val lon = prefs.getFloat("last_longitude", 0f).toDouble()
        
        return if (lat != 0.0 && lon != 0.0) {
            Log.d(TAG, "Using last known coordinates: ($lat, $lon)")
            Pair(lat, lon)
        } else {
            // Default to Kolkata coordinates (from user profile)
            Log.d(TAG, "Using default coordinates: Kolkata")
            Pair(22.5726, 88.3639)
        }
    }
    
    /**
     * Get city name from coordinates
     */
    private suspend fun getCityName(latitude: Double, longitude: Double): String = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            
            if (!addresses.isNullOrEmpty()) {
                val city = addresses[0].locality ?: addresses[0].subAdminArea ?: addresses[0].adminArea
                if (city != null) {
                    prefs.edit().putString("last_location", city).apply()
                    return@withContext city
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting city name", e)
        }
        return@withContext prefs.getString("last_location", null) ?: "your location"
    }
    
    /**
     * Check if query is asking for weather
     */
    fun isWeatherQuery(query: String): Boolean {
        val lower = query.lowercase().trim()
        return lower.contains("weather") || 
               lower.contains("temperature") ||
               lower.contains("forecast") ||
               lower.contains("hot") && (lower.contains("today") || lower.contains("outside")) ||
               lower.contains("cold") && (lower.contains("today") || lower.contains("outside")) ||
               lower.contains("raining") ||
               lower.contains("sunny")
    }
    
    /**
     * Extract location from weather query
     */
    fun extractLocation(query: String): String? {
        val lower = query.lowercase().trim()
        
        // Patterns: "weather in [city]", "weather [city]", "[city] weather"
        val patterns = listOf(
            "weather in (.+?)(?:\\s+today|\\s+tomorrow|$)".toRegex(),
            "weather for (.+?)(?:\\s+today|\\s+tomorrow|$)".toRegex(),
            "(?:today's|tomorrow's) weather in (.+?)$".toRegex(),
            "(.+?) weather(?:\\s+today|\\s+tomorrow|$)".toRegex()
        )
        
        for (pattern in patterns) {
            val match = pattern.find(lower)
            if (match != null) {
                val location = match.groupValues[1].trim()
                if (location.length > 2 && !location.contains("what") && !location.contains("the")) {
                    return location.split(" ")
                        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                }
            }
        }
        
        return null
    }
    
    /**
     * Weather data class
     */
    data class WeatherData(
        val temperature: Double,
        val condition: String,
        val humidity: Int,
        val windSpeed: Double,
        val weatherCode: Int
    )
    
    /**
     * Weather result class
     */
    data class WeatherResult(
        val success: Boolean,
        val message: String,
        val temperature: Double? = null,
        val condition: String? = null,
        val humidity: Int? = null,
        val windSpeed: Double? = null,
        val location: String? = null
    )
    
    companion object {
        private const val TAG = "WeatherService"
    }
}
