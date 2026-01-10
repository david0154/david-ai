package com.davidstudioz.david.features

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

/**
 * Weather & Time Provider
 * Uses Open-Meteo API for real weather data
 * API: https://open-meteo.com/
 */
class WeatherTimeProvider(private val context: Context) {

    private var lastKnownLocation: Location? = null
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var locationListener: LocationListener? = null

    // Default location (Kolkata, India)
    private var latitude = 22.5726
    private var longitude = 88.3639

    /**
     * Get current time
     */
    fun getCurrentTime(): String {
        return dateFormat.format(Date())
    }

    /**
     * Get current date
     */
    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    /**
     * Get weather voice report with Open-Meteo API
     */
    suspend fun getWeatherVoiceReport(): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val weather = getWeatherData()
            weather?.let {
                "Current weather: ${it.condition}, temperature ${it.temperature.toInt()} degrees celsius, humidity ${it.humidity} percent, wind speed ${it.windSpeed.toInt()} kilometers per hour"
            } ?: "Weather data unavailable"
        } catch (e: Exception) {
            "Unable to fetch weather: ${e.message}"
        }
    }

    /**
     * Get forecast voice report
     */
    suspend fun getForecastVoiceReport(days: Int): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val forecast = getForecastData(days)
            if (forecast.isNotEmpty()) {
                val temps = forecast.map { it.temperature.toInt() }
                val minTemp = temps.minOrNull() ?: 0
                val maxTemp = temps.maxOrNull() ?: 0
                "Forecast for next $days days: Temperatures ranging from $minTemp to $maxTemp degrees celsius"
            } else {
                "Forecast unavailable"
            }
        } catch (e: Exception) {
            "Unable to fetch forecast: ${e.message}"
        }
    }

    /**
     * Get weather data from Open-Meteo API
     */
    private suspend fun getWeatherData(): WeatherData? = withContext(Dispatchers.IO) {
        return@withContext try {
            // Update location if available
            getLastLocation()?.let {
                latitude = it.latitude
                longitude = it.longitude
            }

            // Open-Meteo API URL
            val apiUrl = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=$latitude&" +
                    "longitude=$longitude&" +
                    "current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code&" +
                    "timezone=auto"

            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                // Parse JSON response
                val json = JSONObject(response)
                val current = json.getJSONObject("current")

                val temperature = current.getDouble("temperature_2m").toFloat()
                val humidity = current.getInt("relative_humidity_2m")
                val windSpeed = current.getDouble("wind_speed_10m").toFloat()
                val weatherCode = current.getInt("weather_code")

                WeatherData(
                    temperature = temperature,
                    condition = getWeatherCondition(weatherCode),
                    humidity = humidity,
                    windSpeed = windSpeed,
                    location = "Lat: ${latitude.format(2)}, Lon: ${longitude.format(2)}"
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get forecast data from Open-Meteo API
     */
    private suspend fun getForecastData(days: Int): List<WeatherData> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Update location if available
            getLastLocation()?.let {
                latitude = it.latitude
                longitude = it.longitude
            }

            // Open-Meteo API URL for forecast
            val apiUrl = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=$latitude&" +
                    "longitude=$longitude&" +
                    "daily=temperature_2m_max,temperature_2m_min,weather_code&" +
                    "forecast_days=$days&" +
                    "timezone=auto"

            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                // Parse JSON response
                val json = JSONObject(response)
                val daily = json.getJSONObject("daily")
                val tempMax = daily.getJSONArray("temperature_2m_max")
                val weatherCodes = daily.getJSONArray("weather_code")

                val forecast = mutableListOf<WeatherData>()
                for (i in 0 until days.coerceAtMost(tempMax.length())) {
                    forecast.add(
                        WeatherData(
                            temperature = tempMax.getDouble(i).toFloat(),
                            condition = getWeatherCondition(weatherCodes.getInt(i)),
                            humidity = 0,
                            windSpeed = 0f,
                            location = "Day ${i + 1}"
                        )
                    )
                }
                forecast
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Get last known location
     */
    private fun getLastLocation(): Location? {
        return try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Start location updates
     */
    fun startLocationUpdates(callback: (Location) -> Unit) {
        try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        lastKnownLocation = location
                        latitude = location.latitude
                        longitude = location.longitude
                        callback(location)
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    60000, // 1 minute
                    100f,  // 100 meters
                    locationListener!!
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Stop location updates
     */
    fun stopLocationUpdates() {
        try {
            locationListener?.let {
                locationManager.removeUpdates(it)
                locationListener = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Set location manually
     */
    fun setLocation(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
        lastKnownLocation = Location("").apply {
            this.latitude = latitude
            this.longitude = longitude
        }
    }

    /**
     * Convert WMO weather code to readable condition
     * Source: https://open-meteo.com/en/docs
     */
    private fun getWeatherCondition(code: Int): String {
        return when (code) {
            0 -> "Clear sky"
            1, 2, 3 -> "Partly cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rain"
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
     * Format double to specified decimal places
     */
    private fun Double.format(decimals: Int): String {
        return "%.${decimals}f".format(this)
    }
}

/**
 * Weather data model
 */
data class WeatherData(
    val temperature: Float,
    val condition: String,
    val humidity: Int,
    val windSpeed: Float,
    val location: String
)
