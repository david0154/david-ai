package com.davidstudioz.david.features

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Weather & Time Provider
 * Provides current time, weather, and forecasts
 * Uses Open-Meteo API (free, no API key needed)
 */
class WeatherTimeProvider(private val context: Context) {

    private val TAG = "WeatherTimeProvider"
    private val weatherManager = WeatherManager(context)
    private val forecastManager = ForecastManager()
    private var currentLocation: Location? = null

    /**
     * Get current time in HH:MM:SS format
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
     * Get current weather with location (voice-friendly)
     */
    suspend fun getWeatherVoiceReport(): String {
        return weatherManager.getWeatherVoiceReport(useMetric = true)
    }

    /**
     * Get current weather data
     */
    suspend fun getWeatherData(latitude: Double = 0.0, longitude: Double = 0.0): WeatherData? {
        return if (latitude == 0.0 && longitude == 0.0) {
            val location = getCurrentLocation()
            if (location != null) {
                weatherManager.getWeatherData(location.latitude, location.longitude)
            } else {
                null
            }
        } else {
            weatherManager.getWeatherData(latitude, longitude)
        }
    }

    /**
     * Get weather forecast (voice-friendly)
     */
    suspend fun getForecastVoiceReport(days: Int = 3): String {
        val location = getCurrentLocation()
        return if (location != null) {
            forecastManager.getForecastVoiceReport(
                location.latitude,
                location.longitude,
                days
            )
        } else {
            "I couldn't determine your location for the forecast."
        }
    }

    /**
     * Get weather forecast data
     */
    suspend fun getForecastData(latitude: Double = 0.0, longitude: Double = 0.0): ForecastData? {
        val loc = if (latitude == 0.0 && longitude == 0.0) {
            getCurrentLocation()
        } else {
            Location("").apply {
                this.latitude = latitude
                this.longitude = longitude
            }
        }

        return if (loc != null) {
            forecastManager.getForecastData(loc.latitude, loc.longitude)
        } else {
            null
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
            if (location != null && System.currentTimeMillis() - location.time < 10 * 60 * 1000) {
                return location
            }

            // Fall back to network location
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (location != null && System.currentTimeMillis() - location.time < 10 * 60 * 1000) {
                return location
            }

            // Default to Kolkata if no location available
            Log.d(TAG, "Using default location: Kolkata")
            Location("kolkata").apply {
                latitude = 22.5726
                longitude = 88.3639
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting location", e)
            // Default to Kolkata
            Location("kolkata").apply {
                latitude = 22.5726
                longitude = 88.3639
            }
        }
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
                "altitude" to location.altitude,
                "provider" to (location.provider ?: "default")
            )
        } else {
            mapOf("status" to "Location not available")
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
     * Start weather updates
     */
    fun startWeatherUpdates(callback: (String) -> Unit) {
        weatherManager.startLocationUpdates { location ->
            Log.d(TAG, "Location updated, weather will refresh")
        }
    }
}
