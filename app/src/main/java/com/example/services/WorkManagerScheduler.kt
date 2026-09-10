package com.example.services

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.workers.DailyPrayerRescheduleWorker
import com.example.workers.SpiritualReminderWorker
import java.util.concurrent.TimeUnit

object WorkManagerScheduler {

    private const val TAG = "WorkManagerScheduler"
    private const val WORK_SPIRITUAL_REMINDERS = "quran_radio_spiritual_reminders_work"
    private const val WORK_DAILY_PRAYER_RESCHEDULE = "quran_radio_daily_prayer_reschedule_work"

    fun setupPeriodicBackgroundTasks(context: Context) {
        try {
            val workManager = WorkManager.getInstance(context)

            // 1. Periodic Spiritual Reminders every 2 hours (Quran, Live Radio, Tafseer, Khatmah)
            val spiritualWorkRequest = PeriodicWorkRequestBuilder<SpiritualReminderWorker>(
                2, TimeUnit.HOURS,
                15, TimeUnit.MINUTES // Flex interval for power-efficient battery management
            ).build()

            workManager.enqueueUniquePeriodicWork(
                WORK_SPIRITUAL_REMINDERS,
                ExistingPeriodicWorkPolicy.UPDATE,
                spiritualWorkRequest
            )

            // 2. Daily Prayer Reschedule (Every 12 hours as a 2nd safety layer)
            val rescheduleWorkRequest = PeriodicWorkRequestBuilder<DailyPrayerRescheduleWorker>(
                12, TimeUnit.HOURS,
                1, TimeUnit.HOURS // Flex interval
            ).build()

            workManager.enqueueUniquePeriodicWork(
                WORK_DAILY_PRAYER_RESCHEDULE,
                ExistingPeriodicWorkPolicy.UPDATE,
                rescheduleWorkRequest
            )

            Log.d(TAG, "Successfully initialized periodic background WorkManager tasks (2-hour spiritual reminders & 12-hour prayer safety sync)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule background WorkManager tasks", e)
        }
    }
}
