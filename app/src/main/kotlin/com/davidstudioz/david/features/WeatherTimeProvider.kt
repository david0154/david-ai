package com.davidstudioz.david.features

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Weather & Time Provider
 * Provides current time and weather information
 * Uses location for weather API
 */
class WeatherTimeProvider(private val context: Context) {

    private val TAG = "WeatherTimeProvider"
    private var currentLocation: Location? = null

    /**
     * Get current time in formatted string
     */
    fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Get current date
     */
    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Get detailed time information
     */
    fun getDetailedTime(): String {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
        val time = timeFormat.format(Date())
        val date = dateFormat.format(Date())
        return "It's $time on $date"
    }

    /**
     * Get weather information
     * In real app, would call weather API (OpenWeatherMap, etc.)
     */
    suspend fun getWeather(latitude: Double, longitude: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                // Get current location if not provided
                val loc = if (latitude == 0.0 && longitude == 0.0) {
                    getCurrentLocation()
                } else {
                    Location("").apply {
                        this.latitude = latitude
                        this.longitude = longitude
                    }
                }

                if (loc != null) {
                    // Call weather API (mock implementation)
                    "The weather is sunny, 28°C with light winds from the north. Humidity 65%"
                } else {
                    "Unable to fetch weather - location not available"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching weather", e)
                "Weather data unavailable"
            }
        }
    }

    /**
     * Get current location
     */
    private fun getCurrentLocation(): Location? {
        return try {
            if (!hasLocationPermission()) {
                Log.w(TAG, "Location permission not granted")
                return null
            }

            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            
            // Try GPS first
            var location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            
            // Fall back to network location
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
            
            location
        } catch (e: Exception) {
            Log.e(TAG, "Error getting location", e)
            null
        }
    }

    /**
     * Get weather forecast
     */
    fun getWeatherForecast(days: Int = 3): List<String> {
        // Mock forecast data
        return listOf(
            "Tomorrow: Sunny, 30°C",
            "Day after: Cloudy, 27°C",
            "In 3 days: Rainy, 24°C"
        ).take(days)
    }

    /**
     * Convert temperature
     */
    fun convertTemperature(celsius: Float, toFahrenheit: Boolean = false): Float {
        return if (toFahrenheit) {
            (celsius * 9/5) + 32
        } else {
            (celsius - 32) * 5/9
        }
    }

    /**
     * Check location permission
     */
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get location info
     */
    fun getLocationInfo(): Map<String, Any> {
        val location = getCurrentLocation()
        return if (location != null) {
            mapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "accuracy" to location.accuracy,
                "altitude" to location.altitude
            )
        } else {
            mapOf("status" to "Location not available")
        }
    }
}
