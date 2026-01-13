package com.davidstudioz.david.features

import android.content.Context
import android.location.Geocoder
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * WeatherService - Real weather data from Open-Meteo API
 * ✅ Free weather API (no key required)
 * ✅ Current weather conditions
 * ✅ Temperature, humidity, wind speed
 * ✅ Weather descriptions
 * ✅ Location-based weather
 */
class WeatherService(private val context: Context) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val geocoder = Geocoder(context, Locale.getDefault())

    /**
     * Get current weather for a location
     */
    suspend fun getCurrentWeather(locationQuery: String): Result<WeatherResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Fetching weather for: $locationQuery")

            // Get coordinates for location
            val coords = getCoordinates(locationQuery)
            if (coords == null) {
                return@withContext Result.failure(Exception("Could not find location: $locationQuery"))
            }

            val (lat, lon) = coords
            Log.d(TAG, "Coordinates: $lat, $lon")

            // Fetch weather from Open-Meteo API
            val url = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=$lat&longitude=$lon" +
                    "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,apparent_temperature" +
                    "&temperature_unit=celsius" +
                    "&wind_speed_unit=kmh" +
                    "&timezone=auto"

            val request = Request.Builder()
                .url(url)
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful || body == null) {
                return@withContext Result.failure(Exception("Weather API request failed: ${response.code}"))
            }

            // Parse JSON response
            val json = JSONObject(body)
            val current = json.getJSONObject("current")

            val temperature = current.getDouble("temperature_2m")
            val humidity = current.getInt("relative_humidity_2m")
            val windSpeed = current.getDouble("wind_speed_10m")
            val weatherCode = current.getInt("weather_code")
            val feelsLike = current.getDouble("apparent_temperature")

            val condition = getWeatherCondition(weatherCode)
            val description = getWeatherDescription(weatherCode, temperature)

            val weatherResponse = WeatherResponse(
                location = locationQuery,
                temperature = temperature,
                feelsLike = feelsLike,
                condition = condition,
                description = description,
                humidity = humidity,
                windSpeed = windSpeed,
                weatherCode = weatherCode
            )

            Log.d(TAG, "✅ Weather fetched: ${weatherResponse.temperature}°C, ${weatherResponse.condition}")
            Result.success(weatherResponse)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather", e)
            Result.failure(e)
        }
    }

    /**
     * Get coordinates for location name
     */
    private fun getCoordinates(location: String): Pair<Double, Double>? {
        return try {
            // Try geocoding
            val addresses = geocoder.getFromLocationName(location, 1)
            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                Pair(address.latitude, address.longitude)
            } else {
                // Default to some major cities if geocoding fails
                getDefaultCoordinates(location)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Geocoding error", e)
            getDefaultCoordinates(location)
        }
    }

    /**
     * Default coordinates for major cities
     */
    private fun getDefaultCoordinates(location: String): Pair<Double, Double>? {
        val lower = location.lowercase().trim()
        return when {
            lower.contains("kolkata") -> Pair(22.5726, 88.3639)
            lower.contains("delhi") -> Pair(28.6139, 77.2090)
            lower.contains("mumbai") -> Pair(19.0760, 72.8777)
            lower.contains("bangalore") -> Pair(12.9716, 77.5946)
            lower.contains("chennai") -> Pair(13.0827, 80.2707)
            lower.contains("hyderabad") -> Pair(17.3850, 78.4867)
            lower.contains("pune") -> Pair(18.5204, 73.8567)
            lower.contains("ahmedabad") -> Pair(23.0225, 72.5714)
            lower.contains("jaipur") -> Pair(26.9124, 75.7873)
            lower.contains("lucknow") -> Pair(26.8467, 80.9462)
            lower.contains("london") -> Pair(51.5074, -0.1278)
            lower.contains("new york") -> Pair(40.7128, -74.0060)
            lower.contains("paris") -> Pair(48.8566, 2.3522)
            lower.contains("tokyo") -> Pair(35.6762, 139.6503)
            lower.contains("sydney") -> Pair(-33.8688, 151.2093)
            else -> Pair(22.5726, 88.3639) // Default to Kolkata
        }
    }

    /**
     * Convert WMO weather code to condition
     */
    private fun getWeatherCondition(code: Int): String {
        return when (code) {
            0 -> "Clear"
            1, 2, 3 -> "Partly Cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rainy"
            71, 73, 75 -> "Snowy"
            77 -> "Snow Grains"
            80, 81, 82 -> "Rain Showers"
            85, 86 -> "Snow Showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with Hail"
            else -> "Unknown"
        }
    }

    /**
     * Get natural language weather description
     */
    private fun getWeatherDescription(code: Int, temp: Double): String {
        val condition = getWeatherCondition(code)
        val tempDesc = when {
            temp < 10 -> "cold"
            temp < 20 -> "cool"
            temp < 28 -> "pleasant"
            temp < 35 -> "warm"
            else -> "hot"
        }

        return when (code) {
            0 -> "It's a $tempDesc and clear day"
            1, 2, 3 -> "It's $tempDesc with some clouds"
            45, 48 -> "It's foggy and $tempDesc"
            51, 53, 55 -> "Light drizzle, $tempDesc"
            61, 63, 65 -> "It's raining and $tempDesc"
            71, 73, 75 -> "It's snowing and cold"
            80, 81, 82 -> "Rain showers expected, $tempDesc"
            95 -> "Thunderstorm conditions, $tempDesc"
            else -> "Weather is $tempDesc"
        }
    }

    /**
     * Format weather for voice response
     */
    fun formatWeatherForVoice(weather: WeatherResponse): String {
        return "The weather in ${weather.location} is currently ${weather.temperature.toInt()} degrees Celsius. " +
                "${weather.description}. " +
                "Humidity is ${weather.humidity}% with wind speed of ${weather.windSpeed.toInt()} kilometers per hour. " +
                "It feels like ${weather.feelsLike.toInt()} degrees."
    }

    /**
     * Format weather for text display
     */
    fun formatWeatherForText(weather: WeatherResponse): String {
        return """
            🌡️ Weather in ${weather.location}
            
            Temperature: ${weather.temperature.toInt()}°C (feels like ${weather.feelsLike.toInt()}°C)
            Condition: ${weather.condition}
            ${weather.description}
            
            💧 Humidity: ${weather.humidity}%
            💨 Wind Speed: ${weather.windSpeed.toInt()} km/h
        """.trimIndent()
    }

    companion object {
        private const val TAG = "WeatherService"
    }
}

/**
 * Weather response data class
 */
data class WeatherResponse(
    val location: String,
    val temperature: Double,
    val feelsLike: Double,
    val condition: String,
    val description: String,
    val humidity: Int,
    val windSpeed: Double,
    val weatherCode: Int
)
