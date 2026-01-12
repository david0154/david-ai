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
 * WeatherService - Fetch weather data from Open-Meteo API
 * ✅ Uses Open-Meteo API (free, no API key needed)
 * ✅ Gets location-based weather
 * ✅ Returns human-readable weather info
 * ✅ No browser opening - background fetch
 * ✅ Geocoding support for city names
 */
class WeatherService(private val context: Context) {

    /**
     * Get current weather for user's location
     * Returns spoken response like "It's 25 degrees and sunny in Kolkata"
     */
    suspend fun getCurrentWeather(): String = withContext(Dispatchers.IO) {
        try {
            val location = getLastKnownLocation()
            
            if (location == null) {
                return@withContext "I couldn't determine your location. Please enable location services."
            }
            
            val weatherData = fetchWeatherFromOpenMeteo(location.latitude, location.longitude)
            
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
            // First, geocode the city name to get coordinates
            val coords = geocodeCity(city)
            if (coords == null) {
                return@withContext "I couldn't find the location: $city"
            }
            
            // Fetch weather for coordinates
            val weatherData = fetchWeatherFromOpenMeteo(coords.first, coords.second, city)
            
            return@withContext weatherData
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather for city", e)
            return@withContext "I couldn't fetch weather for $city. Please try again."
        }
    }
    
    /**
     * Geocode city name to coordinates using Open-Meteo Geocoding API
     * Returns Pair(latitude, longitude) or null
     */
    private fun geocodeCity(city: String): Pair<Double, Double>? {
        try {
            val encodedCity = URLEncoder.encode(city, "UTF-8")
            val url = URL("https://geocoding-api.open-meteo.com/v1/search?name=$encodedCity&count=1&language=en&format=json")
            
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                
                val json = JSONObject(response)
                val results = json.optJSONArray("results")
                if (results != null && results.length() > 0) {
                    val firstResult = results.getJSONObject(0)
                    val lat = firstResult.getDouble("latitude")
                    val lon = firstResult.getDouble("longitude")
                    return Pair(lat, lon)
                }
            } else {
                connection.disconnect()
            }
            
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error geocoding city", e)
            return null
        }
    }
    
    /**
     * Fetch weather from Open-Meteo API (free, no API key needed)
     * API: https://api.open-meteo.com/v1/forecast
     */
    private fun fetchWeatherFromOpenMeteo(lat: Double, lon: Double, locationName: String = "your location"): String {
        try {
            // Open-Meteo API URL with current weather parameters
            val url = URL(
                "https://api.open-meteo.com/v1/forecast?" +
                "latitude=$lat&longitude=$lon&" +
                "current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&" +
                "temperature_unit=celsius&" +
                "wind_speed_unit=kmh&" +
                "timezone=auto"
            )
            
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                
                return parseOpenMeteoResponse(response, locationName)
            } else {
                connection.disconnect()
                return "Weather service unavailable"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching from Open-Meteo", e)
            return "Unable to fetch weather data"
        }
    }
    
    /**
     * Parse Open-Meteo JSON response into spoken format
     * Example output: "In Kolkata, it's clear with 25 degrees celsius, wind 10 kilometers per hour, humidity 60 percent"
     */
    private fun parseOpenMeteoResponse(jsonResponse: String, locationName: String): String {
        try {
            val json = JSONObject(jsonResponse)
            val current = json.getJSONObject("current")
            
            val temperature = current.getDouble("temperature_2m").toInt()
            val humidity = current.getInt("relative_humidity_2m")
            val windSpeed = current.getDouble("wind_speed_10m").toInt()
            val weatherCode = current.getInt("weather_code")
            
            // Convert WMO weather code to description
            val condition = getWeatherCondition(weatherCode)
            
            return buildString {
                append("In $locationName, it's $condition with $temperature degrees celsius")
                if (windSpeed > 0) {
                    append(", wind $windSpeed kilometers per hour")
                }
                append(", humidity $humidity percent")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Open-Meteo response", e)
            return "Weather data in $locationName is available but format not recognized"
        }
    }
    
    /**
     * Convert WMO Weather Code to human-readable condition
     * WMO Codes: https://open-meteo.com/en/docs
     */
    private fun getWeatherCondition(code: Int): String {
        return when (code) {
            0 -> "clear sky"
            1 -> "mainly clear"
            2 -> "partly cloudy"
            3 -> "overcast"
            45, 48 -> "foggy"
            51, 53, 55 -> "drizzling"
            56, 57 -> "freezing drizzle"
            61, 63, 65 -> "rainy"
            66, 67 -> "freezing rain"
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
     * Get last known location from LocationManager
     */
    private fun getLastKnownLocation(): Location? {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            
            // Try GPS first
            val gpsLocation = try {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } catch (e: SecurityException) {
                Log.w(TAG, "GPS permission denied")
                null
            }
            
            // Fall back to network
            val networkLocation = try {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (e: SecurityException) {
                Log.w(TAG, "Network location permission denied")
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
