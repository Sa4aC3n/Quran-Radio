package com.example.services

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.HaramPreferencesRepository
import com.example.model.HaramSource
import kotlinx.coroutines.flow.first

class HaramLiveSyncWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = HaramPreferencesRepository(context)
        val prefs = repository.preferences.first()

        if (!prefs.isEnabled) {
            return Result.success()
        }

        try {
            val targetSource = if (prefs.source != HaramSource.BOTH) prefs.source else HaramSource.MAKKAH
            val sampleSurah = prefs.favoriteSurahs.firstOrNull() ?: 18

            if (repository.canSendNotification(sampleSurah, targetSource)) {
                HaramLiveNotificationHelper.showHaramNotification(
                    context = context,
                    surahNumber = sampleSurah,
                    source = targetSource
                )
                repository.recordNotificationSent()
                Log.i("HaramLiveSyncWorker", "[HARAM_LIVE] Dispatched live Haram recitation notification for $targetSource")
            }
        } catch (e: Exception) {
            Log.e("HaramLiveSyncWorker", "Error executing HaramLiveSyncWorker: ${e.message}", e)
        }

        return Result.success()
    }
}
