package com.example.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.model.PrayerType

/**
 * Legacy service adapter: delegates immediately to AdhanManager without displaying any notifications.
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
            stopSelf()
            return START_NOT_STICKY
        }

        val prayerTypeId = intent?.getStringExtra("PRAYER_TYPE_ID") ?: PrayerType.FAJR.id
        val prayerType = PrayerType.fromId(prayerTypeId)

        // Delegate to AdhanManager (pauses radio, manages audio focus, plays adhan, resumes radio, NO notifications)
        AdhanManager.getInstance(this).playAdhan(prayerType, isTest = false)
        stopSelf()
        return START_NOT_STICKY
    }
}
