package com.example.meteapp.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ForecastApi {
    @GET("/v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("hourly") hourly: String = "temperature_2m,weathercode",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min",
        @Query("windspeed_unit") windspeedUnit: String = "kmh",
        @Query("models") models: String = "meteofrance_seamless",
        @Query("timezone") timezone: String = "auto"
    ): Response<ForecastResponse>
}

