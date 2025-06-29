package com.example.plantpal.repository


import com.example.plantpal.model.ApiPlant
import com.example.plantpal.data.remote.ApiPlantService
import com.example.plantpal.model.CachedApiPlant
import com.example.plantpal.data.local.CachedApiPlantDao
import com.example.plantpal.util.Resource
import com.example.plantpal.util.toCachedEntity
import javax.inject.Inject


class CachedApiPlantRepository @Inject constructor(
    private val apiService: ApiPlantService,
    private val cachedDao: CachedApiPlantDao
) {
    private val apiKey = "sk-jUJ3682e15df5603a10557"

    suspend fun loadPlantsWithFallback(): Resource<List<CachedApiPlant>> {
        return try {
            val response = apiService.getPlants(apiKey)
            val apiPlants = response.data

            val cached = apiPlants.map { it.toCachedEntity() }

            cachedDao.clearCache()
            cachedDao.insertAll(cached)

            Resource.Success(cached)
        } catch (e: Exception) {

            val fallback = cachedDao.getAllCachedDirect()
            if (fallback.isNotEmpty()) Resource.Success(fallback)
            else Resource.Error("No internet, no plants available.")
        }
    }
    suspend fun getCachedPlants(): List<CachedApiPlant> {
        return cachedDao.getAllCachedDirect()
    }

    suspend fun saveApiPlantsToCache(apiPlants: List<ApiPlant>) {
        val cached = apiPlants.map { it.toCachedEntity() }
        cachedDao.clearCache()
        cachedDao.insertAll(cached)
    }

}
