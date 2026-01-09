package com.davidstudioz.david.features

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName

/**
 * Weather Manager
 * Fetches weather from Open-Meteo API based on device location
 * No API key required - free to use
 */
class WeatherManager(private val context: Context) {

    private val TAG = "WeatherManager"
    private var currentLocation: Location? = null
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val weatherApi = retrofit.create(OpenMeteoAPI::class.java)

    /**
     * Get current weather as voice-friendly string
     */
    suspend fun getWeatherVoiceReport(useMetric: Boolean = true): String {
        return withContext(Dispatchers.IO) {
            try {
                val location = getLastKnownLocation()
                if (location != null) {
                    val weather = fetchWeather(location.latitude, location.longitude)
                    formatWeatherVoiceReport(weather, useMetric)
                } else {
                    "I couldn't determine your location. Please enable location services."
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching weather", e)
                "Sorry, I couldn't fetch the weather right now."
            }
        }
    }

    /**
     * Get weather object with all details
     */
    suspend fun getWeatherData(latitude: Double, longitude: Double): WeatherData? {
        return withContext(Dispatchers.IO) {
            try {
                fetchWeather(latitude, longitude)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching weather", e)
                null
            }
        }
    }

    /**
     * Fetch from Open-Meteo API
     */
    private suspend fun fetchWeather(latitude: Double, longitude: Double): WeatherData? {
        return try {
            val response = weatherApi.getCurrentWeather(
                latitude = latitude,
                longitude = longitude,
                current = "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m,wind_direction_10m",
                timezone = "auto"
            )
            response.apply {
                Log.d(TAG, "Weather fetched: Temp=${current.temperature_2m}°C")
            }
        } catch (e: Exception) {
            Log.e(TAG, "API call failed", e)
            null
        }
    }

    /**
     * Format weather as natural voice response
     */
    private fun formatWeatherVoiceReport(weather: WeatherData?, useMetric: Boolean): String {
        if (weather == null) return "Weather data unavailable."

        val current = weather.current
        val temp = if (useMetric) {
            "${current.temperature_2m.toInt()}°C"
        } else {
            "${(current.temperature_2m * 9/5 + 32).toInt()}°F"
        }

        val condition = getWeatherCondition(current.weather_code)
        val humidity = current.relative_humidity_2m
        val wind = current.wind_speed_10m.toInt()
        val windDir = getWindDirection(current.wind_direction_10m.toInt())

        return "Current weather: It's $temp and $condition. " +
               "Humidity is $humidity percent. " +
               "Winds are $wind kilometers per hour from the $windDir."
    }

    /**
     * Get weather condition from WMO code
     */
    private fun getWeatherCondition(code: Int): String {
        return when (code) {
            0 -> "clear and sunny"
            1, 2 -> "mostly clear"
            3 -> "overcast"
            45, 48 -> "foggy"
            51, 53, 55 -> "drizzling"
            61, 63, 65 -> "rainy"
            71, 73, 75 -> "snowing"
            77 -> "snowing"
            80, 81, 82 -> "heavily raining"
            85, 86 -> "snowing heavily"
            95, 96, 99 -> "thunderstorming"
            else -> "cloudy"
        }
    }

    /**
     * Convert degrees to wind direction
     */
    private fun getWindDirection(degrees: Int): String {
        return when (degrees) {
            in 0..45 -> "North"
            in 46..135 -> "East"
            in 136..225 -> "South"
            in 226..315 -> "West"
            else -> "North"
        }
    }

    /**
     * Get last known location
     */
    private fun getLastKnownLocation(): Location? {
        return try {
            if (!hasLocationPermission()) {
                Log.w(TAG, "Location permission not granted")
                return null
            }

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

            // Default to Kolkata if no location
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
     * Request location updates
     */
    fun startLocationUpdates(callback: (Location) -> Unit) {
        try {
            if (!hasLocationPermission()) {
                Log.w(TAG, "Location permission not granted")
                return
            }

            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    currentLocation = location
                    callback(location)
                    Log.d(TAG, "Location updated: ${location.latitude}, ${location.longitude}")
                }

                override fun onProviderEnabled(provider: String) {
                    Log.d(TAG, "Provider enabled: $provider")
                }

                override fun onProviderDisabled(provider: String) {
                    Log.d(TAG, "Provider disabled: $provider")
                }
            }

            // Request from both GPS and network
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,  // 5 seconds
                0f,    // 0 meters
                locationListener
            )

            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5000,
                0f,
                locationListener
            )

            Log.d(TAG, "Location updates started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting location updates", e)
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
        val location = getLastKnownLocation()
        return if (location != null) {
            mapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "accuracy" to location.accuracy,
                "altitude" to location.altitude,
                "provider" to (location.provider ?: "unknown")
            )
        } else {
            mapOf("status" to "Location not available")
        }
    }
}

/**
 * Open-Meteo API Interface
 */
interface OpenMeteoAPI {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String,
        @Query("timezone") timezone: String
    ): WeatherDataResponse
}

/**
 * API Response Models
 */
data class WeatherDataResponse(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("timezone")
    val timezone: String,
    @SerializedName("current")
    val current: CurrentWeather
)

data class CurrentWeather(
    @SerializedName("temperature_2m")
    val temperature_2m: Double,
    @SerializedName("relative_humidity_2m")
    val relative_humidity_2m: Int,
    @SerializedName("weather_code")
    val weather_code: Int,
    @SerializedName("wind_speed_10m")
    val wind_speed_10m: Double,
    @SerializedName("wind_direction_10m")
    val wind_direction_10m: Double
)

/**
 * Simplified Weather Data class
 */
data class WeatherData(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWeather
) {
    val temperature: Double get() = current.temperature_2m
    val humidity: Int get() = current.relative_humidity_2m
    val weatherCode: Int get() = current.weather_code
    val windSpeed: Double get() = current.wind_speed_10m
    val windDirection: Double get() = current.wind_direction_10m
}
