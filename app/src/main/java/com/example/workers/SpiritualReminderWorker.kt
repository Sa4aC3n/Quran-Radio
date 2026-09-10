package com.example.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.UserPreferencesRepository
import com.example.model.SpiritualReminderBank
import com.example.services.AdhanNotificationHelper
import kotlinx.coroutines.flow.first

class SpiritualReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val userPrefs = UserPreferencesRepository(context)
            val isEnabled = userPrefs.isNotificationsEnabled.first()
            if (!isEnabled) {
                Log.d(TAG, "Spiritual reminders skipped: notifications disabled by user")
                return Result.success()
            }

            // Pick a message from the offline spiritual bank
            val reminder = SpiritualReminderBank.getRandomMessage()
            Log.d(TAG, "Displaying periodic spiritual reminder: ${reminder.title}")
            AdhanNotificationHelper.showSpiritualReminderNotification(context, reminder)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error executing spiritual reminder worker", e)
            Result.success() // avoid aggressive retry loops for background notifications
        }
    }

    companion object {
        private const val TAG = "SpiritualReminderWorker"
    }
}
