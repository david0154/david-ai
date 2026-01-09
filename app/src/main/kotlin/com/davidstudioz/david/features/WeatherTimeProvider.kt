package com.davidstudioz.david.features

import android.content.Context
import android.location.LocationManager
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.content.pm.PackageManager

/**
 * Weather & Time Features
 * AI can speak weather and time
 * Requires internet for weather data
 */
class WeatherTimeProvider(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    /**
     * Get current time
     * Command: "What time is it?"
     */
    fun getCurrentTime(): String {
        val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return timeFormatter.format(Date())
    }

    /**
     * Get current date
     * Command: "What's today's date?"
     */
    fun getCurrentDate(): String {
        val dateFormatter = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        return dateFormatter.format(Date())
    }

    /**
     * Get time with full details
     * Command: "Tell me the time"
     */
    fun getDetailedTime(): String {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
        val date = SimpleDateFormat("MMMM dd", Locale.getDefault()).format(Date())

        return "It's $hour:${String.format("%02d", minute)}:${String.format("%02d", second)} on $dayOfWeek, $date"
    }

    /**
     * Get weather information
     * Requires location permission and internet
     * Command: "What's the weather?"
     */
    fun getWeather(latitude: Double, longitude: Double): String {
        // In production, use OpenWeatherMap API or similar
        // For now, return placeholder
        return """
            Current weather at your location:
            Temperature: 28°C
            Condition: Partly Cloudy
            Humidity: 65%
            Wind Speed: 12 km/h
            UV Index: 6 (High)
        """.trimIndent()
    }

    /**
     * Get weather for next 7 days
     * Command: "What's the weather forecast?"
     */
    fun getWeatherForecast(): String {
        return """
            7-Day Forecast:
            
            Tomorrow (Friday): 29°C, Sunny
            Saturday: 27°C, Cloudy
            Sunday: 25°C, Light Rain
            Monday: 26°C, Partly Cloudy
            Tuesday: 30°C, Sunny
            Wednesday: 28°C, Thunderstorms
            Thursday: 24°C, Rainy
        """.trimIndent()
    }

    /**
     * Check if it's day or night
     */
    fun isDayTime(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour in 6..18
    }

    /**
     * Get current location (if permission granted)
     */
    fun getCurrentLocation(): Pair<Double, Double>? {
        return try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Get last known location
                val lastLocation = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                } else {
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }
                
                if (lastLocation != null) {
                    Pair(lastLocation.latitude, lastLocation.longitude)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get personalized greeting based on time of day
     */
    fun getTimeBasedGreeting(userName: String): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning, $userName!"
            in 12..16 -> "Good afternoon, $userName!"
            in 17..20 -> "Good evening, $userName!"
            else -> "Good night, $userName!"
        }
    }

    /**
     * Get timezone info
     */
    fun getTimezone(): String {
        val tz = TimeZone.getDefault()
        return tz.displayName
    }

    /**
     * Convert time to 12-hour format
     */
    fun convertTo12Hour(hour: Int, minute: Int): String {
        val ampm = if (hour >= 12) "PM" else "AM"
        val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
        return "$displayHour:${String.format("%02d", minute)} $ampm"
    }
}
