package com.example.meteapp.data.remote

// Modèles Kotlin simples compatibles avec Gson (pas d'annotations Moshi)
data class GeocodingResponse(
    val results: List<GeocodingResult>? = null
)

data class GeocodingResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null,
    val timezone: String? = null
)

data class ForecastResponse(
    val current_weather: CurrentWeather? = null,
    val hourly: Hourly? = null,
    val daily: Daily? = null
)

data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    val winddirection: Double? = null,
    val weathercode: Int,
    val time: String
)

data class Hourly(
    val temperature_2m: List<Double>? = null,
    val weathercode: List<Int>? = null,
    val time: List<String>? = null
)

data class Daily(
    val temperature_2m_max: List<Double>? = null,
    val temperature_2m_min: List<Double>? = null,
    val time: List<String>? = null
)
