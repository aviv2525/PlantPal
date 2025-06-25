package com.example.plantpal.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.plantpal.model.FavoritePlant

@Dao
interface FavoritePlantDao {


    @Query("SELECT * FROM FavoritePlantsTable")
    fun getAllFavorites(): LiveData<List<FavoritePlant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plant: FavoritePlant)

    @Query("DELETE FROM FavoritePlantsTable WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Update
    suspend fun update(plant: FavoritePlant)


    @Query("SELECT EXISTS(SELECT 1 FROM FavoritePlantsTable WHERE id = :plantId)")
    suspend fun isFavorite(plantId: Int): Boolean
}
