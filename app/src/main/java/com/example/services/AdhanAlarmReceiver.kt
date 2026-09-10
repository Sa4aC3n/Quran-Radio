package com.example.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.model.PrayerType

class AdhanAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action == "com.example.quranradio.ACTION_STOP_ADHAN") {
            Log.i("AdhanAlarmReceiver", "[ADHAN] Received ACTION_STOP_ADHAN")
            AdhanManager.getInstance(context).stopAdhan()
            AdhanNotificationHelper.cancelAdhanNotification(context)
            return
        }

        if (action == AdhanAlarmScheduler.ACTION_PRAYER_ALARM || action == "com.example.quranradio.ACTION_PRAYER_ALARM") {
            val prayerTypeId = intent.getStringExtra("PRAYER_TYPE_ID") ?: PrayerType.FAJR.id
            val cityName = intent.getStringExtra("CITY_NAME") ?: "موقعي"
            val prayerType = PrayerType.fromId(prayerTypeId)

            Log.i("AdhanAlarmReceiver", "[ADHAN] Received prayer alarm for: ${prayerType.arabicName} in $cityName")

            // Play Adhan directly through AdhanManager (pauses radio, no notifications)
            AdhanManager.getInstance(context).playAdhan(prayerType, isTest = false)

            // Reschedule rolling alarms in background
            AdhanAlarmScheduler.scheduleUpcomingAlarms(context)
        }
    }
}

