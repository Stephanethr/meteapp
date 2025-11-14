package com.example.meteapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.meteapp.data.local.entities.FavoriteCityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {
    @Query("SELECT * FROM favorite_cities ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavoriteCityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(city: FavoriteCityEntity): Long

    @Query("DELETE FROM favorite_cities WHERE id = :id")
    suspend fun deleteById(id: Long)
}

