package com.example.meteapp.data.repository

import android.content.Context
import com.example.meteapp.data.local.AppDatabase
import com.example.meteapp.data.local.entities.FavoriteCityEntity
import com.example.meteapp.data.local.entities.WeatherCacheEntity
import com.example.meteapp.data.remote.ForecastResponse
import com.example.meteapp.data.remote.GeocodingResponse
import com.example.meteapp.di.NetworkProvider
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow

class WeatherRepository(context: Context) {
    private val db = AppDatabase.create(context)
    private val cityDao = db.cityDao()
    private val weatherDao = db.weatherDao()
    private val geocodingApi = NetworkProvider.provideGeocodingApi()
    private val forecastApi = NetworkProvider.provideForecastApi()
    private val gson = Gson()

    // TTL for current weather: 1 hour
    private val TTL_CURRENT = 60 * 60 * 1000L

    fun observeFavorites(): Flow<List<FavoriteCityEntity>> = cityDao.observeAll()

    suspend fun addFavorite(city: FavoriteCityEntity): Long = cityDao.insert(city)

    suspend fun removeFavorite(id: Long) = cityDao.deleteById(id)

    suspend fun searchCity(query: String): Result<GeocodingResponse> {
        return try {
            val resp = geocodingApi.search(query)
            if (resp.isSuccessful) {
                val body = resp.body() ?: GeocodingResponse()
                Result.Success(body)
            } else {
                Result.Error("HTTP ${resp.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "network error")
        }
    }

    suspend fun getWeather(lat: Double, lon: Double, forceRefresh: Boolean = false): Result<ForecastResponse> {
        val key = "${lat}_${lon}"
        try {
            val cached = weatherDao.getByKey(key)
            val now = System.currentTimeMillis()
            if (!forceRefresh && cached != null && (now - cached.timestampMillis) < TTL_CURRENT) {
                // parse cached payload
                if (cached.payloadJson.isNotEmpty()) {
                    val parsed = gson.fromJson(cached.payloadJson, ForecastResponse::class.java)
                    return Result.Success(parsed)
                }
            }

            val resp = forecastApi.getForecast(lat, lon)
            if (resp.isSuccessful) {
                val body = resp.body() ?: ForecastResponse()
                val json = gson.toJson(body)
                weatherDao.insert(WeatherCacheEntity(key = key, payloadJson = json, timestampMillis = System.currentTimeMillis(), type = "current"))
                return Result.Success(body)
            } else {
                // if network fails, return cached if available
                if (cached != null && cached.payloadJson.isNotEmpty()) {
                    val parsed = gson.fromJson(cached.payloadJson, ForecastResponse::class.java)
                    return Result.Success(parsed)
                }
                return Result.Error("HTTP ${resp.code()}")
            }
        } catch (e: Exception) {
            // network error -> return cache if present
            val cached = weatherDao.getByKey(key)
            if (cached != null && cached.payloadJson.isNotEmpty()) {
                val parsed = gson.fromJson(cached.payloadJson, ForecastResponse::class.java)
                return Result.Success(parsed)
            }
            return Result.Error(e.message ?: "network error")
        }
    }
}

sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}
