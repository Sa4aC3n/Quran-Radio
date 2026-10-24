package com.example.services

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.example.model.PrayerType

/**
 * Foreground Service for reliable Adhan playback in background and Doze mode.
 */
class AdhanPlaybackService : Service() {

    companion object {
        const val ACTION_STOP_ADHAN = "com.example.quranradio.ACTION_STOP_ADHAN"
        const val ACTION_START_ADHAN = "com.example.quranradio.ACTION_START_ADHAN"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_ADHAN) {
            AdhanManager.getInstance(this).stopAdhan()
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val prayerTypeId = intent?.getStringExtra("PRAYER_TYPE_ID") ?: PrayerType.FAJR.id
        val cityName = intent?.getStringExtra("CITY_NAME") ?: "موقعي"
        val prayerType = PrayerType.fromId(prayerTypeId)

        val notification = AdhanNotificationHelper.buildAdhanNotification(
            context = this,
            prayerType = prayerType,
            cityName = cityName
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    AdhanNotificationHelper.NOTIFICATION_ADHAN_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(AdhanNotificationHelper.NOTIFICATION_ADHAN_ID, notification)
            }
        } catch (e: Exception) {
            android.util.Log.e("AdhanPlaybackService", "Failed startForeground: ${e.message}", e)
        }

        AdhanManager.getInstance(this).playAdhan(prayerType, isTest = false)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}
