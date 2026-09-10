package com.example.services

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.HaramPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HaramNotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DISABLE_HARAM_NOTIFICATIONS = "com.example.quranradio.ACTION_DISABLE_HARAM_NOTIFICATIONS"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == ACTION_DISABLE_HARAM_NOTIFICATIONS) {
            val pendingResult = goAsync()
            val repository = HaramPreferencesRepository(context)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    repository.setEnabled(false)
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(HaramLiveNotificationHelper.NOTIFICATION_HARAM_LIVE_ID)

                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            context,
                            "تم إيقاف إشعارات «الآن يُقرأ في الحرم» بنجاح",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
