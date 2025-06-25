package com.example.plantpal.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.plantpal.model.CachedApiPlant

@Dao
interface CachedApiPlantDao {

    @Query("SELECT * FROM CachedApiPlantsTable")
    fun getAllCached(): LiveData<List<CachedApiPlant>>

    @Query("SELECT * FROM CachedApiPlantsTable")
    suspend fun getAllCachedDirect(): List<CachedApiPlant>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plants: List<CachedApiPlant>)

    @Query("DELETE FROM CachedApiPlantsTable")
    suspend fun clearCache()
}
