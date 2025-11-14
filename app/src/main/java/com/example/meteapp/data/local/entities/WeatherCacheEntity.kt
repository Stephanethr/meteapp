package com.example.meteapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val key: String, // e.g., "lat_lon" or cityId
    val payloadJson: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val type: String = "current"
)

