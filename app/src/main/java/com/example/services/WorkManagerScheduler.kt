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
    private const val WORK_HARAM_LIVE_SYNC = "quran_radio_haram_live_sync_work"

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

            // 3. Periodic Haram Live Recitation Sync (Every 1 hour)
            val haramWorkRequest = PeriodicWorkRequestBuilder<HaramLiveSyncWorker>(
                1, TimeUnit.HOURS,
                15, TimeUnit.MINUTES
            ).build()

            workManager.enqueueUniquePeriodicWork(
                WORK_HARAM_LIVE_SYNC,
                ExistingPeriodicWorkPolicy.KEEP,
                haramWorkRequest
            )

            Log.d(TAG, "Successfully initialized periodic background WorkManager tasks (reminders, prayer safety sync, and Haram live sync)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule background WorkManager tasks", e)
        }
    }
}
