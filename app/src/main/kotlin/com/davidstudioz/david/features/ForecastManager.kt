package com.davidstudioz.david.features

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName

/**
 * Forecast Manager
 * Provides multi-day weather forecasts using Open-Meteo API
 */
class ForecastManager {

    private val TAG = "ForecastManager"
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val forecastApi = retrofit.create(ForecastAPI::class.java)

    /**
     * Get 7-day forecast as voice-friendly string
     */
    suspend fun getForecastVoiceReport(latitude: Double, longitude: Double, days: Int = 3): String {
        return withContext(Dispatchers.IO) {
            try {
                val forecast = fetchForecast(latitude, longitude)
                formatForecastVoiceReport(forecast, days)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching forecast", e)
                "Sorry, I couldn't fetch the forecast right now."
            }
        }
    }

    /**
     * Get forecast data
     */
    suspend fun getForecastData(latitude: Double, longitude: Double): ForecastData? {
        return withContext(Dispatchers.IO) {
            try {
                fetchForecast(latitude, longitude)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching forecast", e)
                null
            }
        }
    }

    /**
     * Fetch from Open-Meteo API
     */
    private suspend fun fetchForecast(latitude: Double, longitude: Double): ForecastData? {
        return try {
            val response = forecastApi.getForecast(
                latitude = latitude,
                longitude = longitude,
                daily = "temperature_2m_max,temperature_2m_min,weather_code,wind_speed_10m_max",
                timezone = "auto",
                forecastDays = 7
            )
            Log.d(TAG, "Forecast fetched for $latitude, $longitude")
            response
        } catch (e: Exception) {
            Log.e(TAG, "API call failed", e)
            null
        }
    }

    /**
     * Format forecast as natural voice response
     */
    private fun formatForecastVoiceReport(forecast: ForecastData?, days: Int): String {
        if (forecast == null) return "Forecast data unavailable."

        val daily = forecast.daily
        val report = StringBuilder("Here's the forecast: ")

        repeat(days.coerceAtMost(daily.time.size)) { index ->
            val date = daily.time[index]
            val maxTemp = daily.temperature_2m_max[index].toInt()
            val minTemp = daily.temperature_2m_min[index].toInt()
            val code = daily.weather_code[index]
            val condition = getWeatherConditionShort(code)
            val dayName = getDayName(index)

            report.append("$dayName: $condition with a high of $maxTemp and low of $minTemp degrees. ")
        }

        return report.toString()
    }

    /**
     * Get short weather condition
     */
    private fun getWeatherConditionShort(code: Int): String {
        return when (code) {
            0 -> "sunny"
            1, 2, 3 -> "cloudy"
            45, 48 -> "foggy"
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> "rainy"
            71, 73, 75, 85, 86 -> "snowy"
            95, 96, 99 -> "thunderstorms"
            else -> "cloudy"
        }
    }

    /**
     * Get day name from index
     */
    private fun getDayName(index: Int): String {
        return when (index) {
            0 -> "Today"
            1 -> "Tomorrow"
            2 -> "The day after tomorrow"
            3 -> "In three days"
            4 -> "In four days"
            5 -> "In five days"
            6 -> "In six days"
            else -> "Day ${index + 1}"
        }
    }

    /**
     * Format forecast as list of strings
     */
    fun formatForecastList(forecast: ForecastData?, days: Int = 7): List<String> {
        if (forecast == null) return emptyList()

        val daily = forecast.daily
        return (0 until days.coerceAtMost(daily.time.size)).map { index ->
            val date = daily.time[index]
            val maxTemp = daily.temperature_2m_max[index].toInt()
            val minTemp = daily.temperature_2m_min[index].toInt()
            val code = daily.weather_code[index]
            val condition = getWeatherConditionShort(code)
            val wind = daily.wind_speed_10m_max[index].toInt()

            "$date: $condition, High: $maxTemp°C, Low: $minTemp°C, Wind: $wind km/h"
        }
    }
}

/**
 * Forecast API Interface
 */
interface ForecastAPI {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String,
        @Query("timezone") timezone: String,
        @Query("forecast_days") forecastDays: Int = 7
    ): ForecastData
}

/**
 * Forecast Response Models
 */
data class ForecastData(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("timezone")
    val timezone: String,
    @SerializedName("daily")
    val daily: DailyForecast
)

data class DailyForecast(
    @SerializedName("time")
    val time: List<String>,
    @SerializedName("temperature_2m_max")
    val temperature_2m_max: List<Double>,
    @SerializedName("temperature_2m_min")
    val temperature_2m_min: List<Double>,
    @SerializedName("weather_code")
    val weather_code: List<Int>,
    @SerializedName("wind_speed_10m_max")
    val wind_speed_10m_max: List<Double>
)
