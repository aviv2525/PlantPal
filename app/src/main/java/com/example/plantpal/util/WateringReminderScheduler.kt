package com.example.plantpal.util

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.plantpal.workers.WateringReminderWorker
import java.util.concurrent.TimeUnit

class WateringReminderScheduler(private val context: Context) {
    private val workManager = WorkManager.getInstance(context)
    private val TAG = "WateringReminderScheduler"

    companion object {
        const val TEST_DELAY_SECONDS = 10L
        const val FREQUENT_DAYS = 2L
        const val AVERAGE_DAYS = 5L
        const val MINIMUM_DAYS = 10L
        const val DEFAULT_DAYS = 7L
    }

    fun scheduleWateringReminder(plantName: String, wateringFrequency: String) {
        try {
            val workRequest = createWorkRequest(plantName, wateringFrequency)
            workManager.enqueueUniquePeriodicWork(
                "watering_reminder_$plantName",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling reminder for $plantName", e)
        }
    }

    fun cancelWateringReminder(plantName: String) {
        try {
            workManager.cancelUniqueWork("watering_reminder_$plantName")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling reminder for $plantName", e)
        }
    }

    private fun createWorkRequest(plantName: String, wateringFrequency: String): PeriodicWorkRequest {
        val intervalDays = when (wateringFrequency.lowercase()) {
            "frequent" -> FREQUENT_DAYS
            "average" -> AVERAGE_DAYS
            "minimum" -> MINIMUM_DAYS
            else -> DEFAULT_DAYS
        }

        val inputData = Data.Builder()
            .putString(WateringReminderWorker.PLANT_NAME_KEY, plantName)
            .build()

        return PeriodicWorkRequest.Builder(WateringReminderWorker::class.java, intervalDays, TimeUnit.DAYS)
            .setInputData(inputData)
            .addTag("watering_reminder")
            .addTag(plantName)
            .addTag(wateringFrequency)
            .build()
    }

    fun scheduleTestReminder(plantName: String) {
        try {
            val inputData = Data.Builder()
                .putString(WateringReminderWorker.PLANT_NAME_KEY, plantName)
                .build()

            val testRequest = OneTimeWorkRequestBuilder<WateringReminderWorker>()
                .setInitialDelay(TEST_DELAY_SECONDS, TimeUnit.SECONDS)
                .setInputData(inputData)
                .addTag("test_reminder")
                .build()
            workManager.enqueue(testRequest)
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling test reminder for $plantName", e)
        }
    }

    fun getWorkInfosForPlant(plantName: String): LiveData<List<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkLiveData("watering_reminder_$plantName")
    }


    fun getWateringRemindersLiveData(): LiveData<List<WorkInfo>> {
        return workManager.getWorkInfosByTagLiveData("watering_reminder")
    }
} 