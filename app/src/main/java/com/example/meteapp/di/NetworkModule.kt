package com.example.meteapp.di

import com.example.meteapp.data.remote.ForecastApi
import com.example.meteapp.data.remote.GeocodingApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Provider simplifié sans Moshi/OkHttp explicite pour éviter erreurs de dépendances lors de la compilation initiale.
object NetworkProvider {

    fun provideRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun provideForecastApi(): ForecastApi {
        val retrofit = provideRetrofit("https://api.open-meteo.com")
        return retrofit.create(ForecastApi::class.java)
    }

    fun provideGeocodingApi(): GeocodingApi {
        val retrofit = provideRetrofit("https://geocoding-api.open-meteo.com")
        return retrofit.create(GeocodingApi::class.java)
    }
}
