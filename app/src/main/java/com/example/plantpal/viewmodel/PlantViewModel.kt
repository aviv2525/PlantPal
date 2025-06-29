package com.example.plantpal.viewmodel


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.example.plantpal.ui.details.ApiPlantDetails
import com.example.plantpal.model.ApiPlant
import com.example.plantpal.model.CachedApiPlant
import com.example.plantpal.repository.PlantRepository
import com.example.plantpal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel

import com.example.plantpal.repository.CachedApiPlantRepository
import com.example.plantpal.util.WateringReminderScheduler
import com.example.plantpal.util.toApiPlant


import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlantViewModel @Inject constructor(
    private val repository: PlantRepository,
    private val cachedRepository: CachedApiPlantRepository,
    private val reminderScheduler: WateringReminderScheduler

) : ViewModel() {


    private val _plantList = MutableLiveData<Resource<List<ApiPlant>>>()
    val plantList: LiveData<Resource<List<ApiPlant>>> = _plantList

    private val _cachedPlants = MutableLiveData<Resource<List<CachedApiPlant>>>()
    val cachedPlants: LiveData<Resource<List<CachedApiPlant>>> = _cachedPlants

    private val _plantDetails = MutableLiveData<Resource<ApiPlantDetails>>()
    val plantDetails: LiveData<Resource<ApiPlantDetails>> = _plantDetails

    var selectedFilter: Int? = null


    private val apiKey = "sk-jUJ3682e15df5603a10557"

    val activeReminders = reminderScheduler.getWateringRemindersLiveData()

    init {
        fetchPlantsCacheFirst()
    }



    fun fetchPlantsCacheFirst() {
        viewModelScope.launch {
            _plantList.value = Resource.Loading()

            // 1. טען מהקאש מיד
            val cached = cachedRepository.getCachedPlants()
            if (cached.isNotEmpty()) {
                _plantList.value = Resource.Success(cached.map { it.toApiPlant() })
            }

            // 2. נסה לרענן מהאינטרנט ולעדכן גם את הקאש וגם את התצוגה
            try {
                val response = repository.getPlants(apiKey)
                val apiPlants = response.data
                _plantList.value = Resource.Success(apiPlants)
                cachedRepository.saveApiPlantsToCache(apiPlants)
            } catch (e: Exception) {
                if (cached.isEmpty()) {
                    _plantList.value = Resource.Error("No internet, no information available.")
                } else {
                    // השאר את הקאש על המסך
                }
            }
        }
    }



    fun fetchPlants(indoor: Int? = null) {
        selectedFilter = indoor

        viewModelScope.launch {
            _plantList.value = Resource.Loading()
            try {
                val response = repository.getPlants(indoor,apiKey)
                val result = response.data

                _plantList.value = Resource.Success(response.data)
            } catch (e: Exception) {
                _plantList.value = Resource.Error("Couldn't filter: ${e.message}")
            }
        }
    }
    

    fun fetchPlantDetails(id: Int) {
        viewModelScope.launch {
            _plantDetails.value = Resource.Loading() // ⏳

            try {
                val details = repository.getPlantDetails(id, apiKey)
                _plantDetails.value = Resource.Success(details) // ✅
            } catch (e: Exception) {
                _plantDetails.value = Resource.Error("Failed to load: ${e.message}")
            }
        }
    }

    fun scheduleWateringReminder(plantName: String, wateringFrequency: String) {
        reminderScheduler.scheduleWateringReminder(plantName, wateringFrequency)
    }

    fun cancelWateringReminder(plantName: String) {
        reminderScheduler.cancelWateringReminder(plantName)
    }

    fun scheduleTestReminder(plantName: String) {
        reminderScheduler.scheduleTestReminder(plantName)
    }

    fun getReminderForPlant(plantName: String): LiveData<List<WorkInfo>> {
        return reminderScheduler.getWorkInfosForPlant(plantName)
    }


}
