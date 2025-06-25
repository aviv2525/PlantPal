package com.example.plantpal.di

import android.content.Context
import androidx.room.Room
import com.example.plantpal.data.local.AppDatabase
import com.example.plantpal.data.local.FavoritePlantDao
import com.example.plantpal.data.local.CachedApiPlantDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "plant_pal_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideFavoritePlantDao(database: AppDatabase): FavoritePlantDao {
        return database.favoritePlantDao()
    }

    @Provides
    fun provideCachedApiPlantDao(database: AppDatabase): CachedApiPlantDao {
        return database.cachedApiPlantDao()
    }

}
