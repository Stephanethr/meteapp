package com.example.meteapp.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.meteapp.data.local.dao.CityDao
import com.example.meteapp.data.local.dao.WeatherDao
import com.example.meteapp.data.local.entities.FavoriteCityEntity
import com.example.meteapp.data.local.entities.WeatherCacheEntity

@Database(entities = [FavoriteCityEntity::class, WeatherCacheEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao
    abstract fun weatherDao(): WeatherDao

    companion object {
        private const val DB_NAME = "meteapp.db"

        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}

