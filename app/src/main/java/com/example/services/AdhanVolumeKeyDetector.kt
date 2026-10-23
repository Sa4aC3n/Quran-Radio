package com.example.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Intelligent hardware volume key detector for Adhan playback.
 * Detects double-press on physical volume buttons (Up or Down) while the Adhan
 * is actively playing to instantly mute / stop the Adhan, even when the screen is locked
 * or the phone is in the user's pocket.
 */
class AdhanVolumeKeyDetector(private val context: Context) {

    private var isListening = false
    private var lastEventTimestamp: Long = 0L
    private var onDoublePressCallback: (() -> Unit)? = null

    private val handler = Handler(Looper.getMainLooper())

    // 1. System broadcast receiver for volume changes
    private val volumeChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            if (intent?.action == "android.media.VOLUME_CHANGED_ACTION") {
                checkDoublePress("BroadcastReceiver")
            }
        }
    }

    // 2. ContentObserver on system volume settings for immediate background trigger
    private val volumeSettingsObserver = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            checkDoublePress("ContentObserver")
        }
    }

    @Synchronized
    fun startListening(onDoublePress: () -> Unit) {
        if (isListening) return
        isListening = true
        onDoublePressCallback = onDoublePress
        lastEventTimestamp = 0L

        try {
            val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
            ContextCompat.registerReceiver(
                context,
                volumeChangeReceiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
        } catch (e: Exception) {
            Log.w("AdhanVolumeDetector", "Failed to register volume receiver: ${e.message}")
        }

        try {
            context.contentResolver.registerContentObserver(
                Settings.System.CONTENT_URI,
                true,
                volumeSettingsObserver
            )
        } catch (e: Exception) {
            Log.w("AdhanVolumeDetector", "Failed to register content observer: ${e.message}")
        }

        Log.d("AdhanVolumeDetector", "[ADHAN] Volume key double-press detector started")
    }

    @Synchronized
    fun stopListening() {
        if (!isListening) return
        isListening = false
        onDoublePressCallback = null
        lastEventTimestamp = 0L

        try {
            context.unregisterReceiver(volumeChangeReceiver)
        } catch (_: Exception) {}

        try {
            context.contentResolver.unregisterContentObserver(volumeSettingsObserver)
        } catch (_: Exception) {}

        Log.d("AdhanVolumeDetector", "[ADHAN] Volume key double-press detector stopped")
    }

    @Synchronized
    fun onKeyEvent(): Boolean {
        if (!isListening) return false
        return checkDoublePress("KeyEvent")
    }

    @Synchronized
    private fun checkDoublePress(source: String): Boolean {
        if (!isListening) return false
        val now = System.currentTimeMillis()
        val delta = now - lastEventTimestamp

        // Debounce: multiple rapid callbacks within 120ms from a single button press
        if (delta < 120L) {
            return false
        }

        lastEventTimestamp = now

        // If second press happened within 150ms to 1200ms -> Double press detected!
        if (delta in 150L..1200L) {
            Log.i("AdhanVolumeDetector", "[ADHAN] Hardware volume button double-pressed via $source (delta: ${delta}ms)! Triggering Adhan stop.")
            lastEventTimestamp = 0L
            val callback = onDoublePressCallback
            stopListening()
            callback?.invoke()
            return true
        }

        return false
    }
}
