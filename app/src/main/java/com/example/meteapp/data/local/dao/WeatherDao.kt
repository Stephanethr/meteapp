package com.example.meteapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.meteapp.data.local.entities.WeatherCacheEntity

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_cache WHERE key = :key LIMIT 1")
    suspend fun getByKey(key: String): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache WHERE timestampMillis < :minTimestamp")
    suspend fun purgeOlderThan(minTimestamp: Long)
}

