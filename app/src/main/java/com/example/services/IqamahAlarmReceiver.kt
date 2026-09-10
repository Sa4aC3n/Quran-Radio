package com.example.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.model.PrayerType

class IqamahAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerTypeId = intent.getStringExtra("PRAYER_TYPE_ID") ?: PrayerType.FAJR.id
        val cityName = intent.getStringExtra("CITY_NAME") ?: "موقعي"
        val prayerType = PrayerType.fromId(prayerTypeId)

        AdhanNotificationHelper.showIqamahNotification(
            context = context,
            prayerType = prayerType,
            cityName = cityName
        )
    }
}
