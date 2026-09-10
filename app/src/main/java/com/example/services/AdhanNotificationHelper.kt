package com.example.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.model.PrayerType
import com.example.model.SpiritualReminderMessage
import kotlinx.coroutines.flow.first

object AdhanNotificationHelper {

    // Versioned Channel IDs so changes to sound / attributes create fresh channels on Android 8+
    const val CHANNEL_ADHAN_ID = "quran_radio_adhan_v4"
    const val CHANNEL_IQAMAH_ID = "quran_radio_iqamah_v4"
    const val CHANNEL_REMINDER_ID = "quran_radio_spiritual_reminder_v4"

    const val NOTIFICATION_ADHAN_ID = 8801
    const val NOTIFICATION_IQAMAH_ID = 8802
    const val NOTIFICATION_REMINDER_BASE_ID = 8900

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Delete old channel versions if they exist
            try {
                notificationManager.deleteNotificationChannel("quran_radio_adhan_channel")
                notificationManager.deleteNotificationChannel("quran_radio_adhan_channel_v2")
                notificationManager.deleteNotificationChannel("quran_radio_adhan_v3")
                notificationManager.deleteNotificationChannel("quran_radio_iqamah_channel_v2")
                notificationManager.deleteNotificationChannel("quran_radio_iqamah_v3")
                notificationManager.deleteNotificationChannel("quran_radio_reminders_v1")
                notificationManager.deleteNotificationChannel("quran_radio_spiritual_reminder_v3")
            } catch (e: Exception) {
                // Ignore
            }

            // 1. Adhan Channel (High Importance for Foreground Service, Heads-up & Alarms)
            val adhanChannel = NotificationChannel(
                CHANNEL_ADHAN_ID,
                "تنبيهات الأذان ومواقيت الصلاة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تشغيل صوت الأذان والتنبيه الفوري بدخول وقت الصلاة"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
                setSound(null, null) // Audio is managed through AdhanAudioPlayer with the selected Muezzin sound
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
                enableLights(true)
                lightColor = Color.parseColor("#0284C7")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setAllowBubbles(true)
                }
            }

            // 2. Iqamah Channel
            val iqamahChannel = NotificationChannel(
                CHANNEL_IQAMAH_ID,
                "تنبيهات إقامة الصلاة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيه بقرب إقامة الصلاة في المسجد"
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
                enableLights(true)
                lightColor = Color.parseColor("#059669")
            }

            // 3. Spiritual Reminders Channel (with custom compressed notification chime)
            val reminderSoundUri = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.reminder_notification_sound}")
            val reminderAudioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDER_ID,
                "تذكيرات القرآن والإذاعات والتفسير",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "إشعارات تشجيعية لسماع القرآن الكريم وتدبر الآيات والبث المباشر"
                enableVibration(true)
                setSound(reminderSoundUri, reminderAudioAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(adhanChannel)
            notificationManager.createNotificationChannel(iqamahChannel)
            notificationManager.createNotificationChannel(reminderChannel)
        }
    }

    /**
     * Builds the active Adhan notification (heads-up, high priority, with Stop button).
     */
    fun buildAdhanNotification(
        context: Context,
        prayerType: PrayerType,
        cityName: String
    ): Notification {
        createNotificationChannels(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO", "prayer")
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            101,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop Action Broadcast PendingIntent
        val stopIntent = Intent(context, AdhanAlarmReceiver::class.java).apply {
            action = "com.example.quranradio.ACTION_STOP_ADHAN"
            setPackage(context.packageName)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            102,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prayerName = prayerType.arabicName
        val title = "حان الآن موعد أذان $prayerName 🕌"
        val text = "الله أكبر الله أكبر — التوقيت المحلي لمدينة $cityName وما جاورها"
        val bigText = "الله أكبر الله أكبر — حان الآن موعد أذان $prayerName حسب التوقيت المحلي لمدينة $cityName وما جاورها.\nاضغط أدناه لإيقاف صوت الأذان فوراً."

        return NotificationCompat.Builder(context, CHANNEL_ADHAN_ID)
            .setSmallIcon(R.drawable.ic_adhan_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(title)
                    .bigText(bigText)
            )
            .setColor(0xFF0284C7.toInt()) // SkyBlue Primary theme
            .setColorized(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(mainPendingIntent)
            .setDeleteIntent(stopPendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                "إيقاف صوت الأذان ⏹️",
                stopPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_view,
                "فتح التطبيق 🕌",
                mainPendingIntent
            )
            .build()
    }

    /**
     * Shows a persistent post-Adhan notification that remains visible after playback finishes.
     */
    fun showAdhanCompletedNotification(
        context: Context,
        prayerType: PrayerType,
        cityName: String
    ) {
        createNotificationChannels(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO", "prayer")
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            103,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prayerName = prayerType.arabicName
        val title = "انتهى أذان $prayerName 🕌"
        val text = "أعظم الله أجركم وتقبل طاعتكم — $cityName"

        val notification = NotificationCompat.Builder(context, CHANNEL_ADHAN_ID)
            .setSmallIcon(R.drawable.ic_adhan_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setColor(0xFF0284C7.toInt())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent)
            .build()

        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ADHAN_ID, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Direct method to display Adhan notification instantly.
     */
    fun showAdhanNotification(
        context: Context,
        prayerType: PrayerType,
        cityName: String
    ) {
        createNotificationChannels(context)
        val notification = buildAdhanNotification(context, prayerType, cityName)
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ADHAN_ID, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Cancels the active Adhan notification.
     */
    fun cancelAdhanNotification(context: Context) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ADHAN_ID)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showIqamahNotification(
        context: Context,
        prayerType: PrayerType,
        cityName: String
    ) {
        createNotificationChannels(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO", "prayer")
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            201,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prayerName = prayerType.arabicName
        val title = "حان الآن وقت إقامة صلاة $prayerName 🤲"
        val text = "أقم صلاتك يرحمك الله — $cityName"

        val notification = NotificationCompat.Builder(context, CHANNEL_IQAMAH_ID)
            .setSmallIcon(R.drawable.ic_adhan_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent)
            .build()

        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_IQAMAH_ID + prayerType.ordinal, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showSpiritualReminderNotification(
        context: Context,
        reminder: SpiritualReminderMessage
    ) {
        createNotificationChannels(context)

        val reminderSoundUri = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.reminder_notification_sound}")

        val deepLinkIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO", reminder.targetDeepLink)
            if (reminder.stationId != null) {
                putExtra("STATION_ID", reminder.stationId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reminder.id.hashCode(),
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDER_ID)
            .setSmallIcon(R.drawable.ic_adhan_notification)
            .setContentTitle(reminder.title)
            .setContentText(reminder.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(reminder.body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSound(reminderSoundUri)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notifId = NOTIFICATION_REMINDER_BASE_ID + (System.currentTimeMillis() % 1000).toInt()
            notificationManager.notify(notifId, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun isNotificationAllowed(
        context: Context,
        prayerType: PrayerType? = null
    ): Boolean {
        return try {
            val userPrefs = com.example.data.UserPreferencesRepository(context)
            val isMasterEnabled = userPrefs.isNotificationsEnabled.first()
            if (!isMasterEnabled) return false

            val timeWindow = userPrefs.notificationTimeWindow.first()
            val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

            when (timeWindow) {
                "daytime_only" -> {
                    if (currentHour < 4 || currentHour >= 22) return false
                }
                "quiet_night" -> {
                    if (currentHour >= 23 || currentHour < 5) return false
                }
                "custom_hours" -> {
                    if (currentHour < 7 || currentHour >= 22) return false
                }
            }

            val frequency = userPrefs.notificationFrequency.first()
            if (prayerType != null) {
                when (frequency) {
                    "once_daily" -> {
                        if (prayerType != PrayerType.FAJR) return false
                    }
                    "twice_daily" -> {
                        if (prayerType != PrayerType.FAJR && prayerType != PrayerType.MAGHRIB) return false
                    }
                }
            } else {
                if (frequency == "prayers_5") return false
            }

            true
        } catch (e: Exception) {
            true
        }
    }
}

