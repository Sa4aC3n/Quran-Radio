package com.example.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.services.AdhanAlarmScheduler

class DailyPrayerRescheduleWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Executing periodic background prayer time re-calculation and alarm synchronization (2nd safety layer)")
            AdhanAlarmScheduler.scheduleUpcomingAlarms(context)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error rescheduling upcoming prayer alarms", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "PrayerRescheduleWorker"
    }
}
