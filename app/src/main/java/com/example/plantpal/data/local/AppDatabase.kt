package com.example.plantpal.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.plantpal.model.CachedApiPlant
import com.example.plantpal.model.FavoritePlant


@Database(entities = [FavoritePlant::class, CachedApiPlant::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritePlantDao(): FavoritePlantDao
    abstract fun cachedApiPlantDao(): CachedApiPlantDao


}
