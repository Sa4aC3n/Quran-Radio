package com.example.services

import android.content.Context
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

        // Feature is active and configured
        return Result.success()
    }
}
