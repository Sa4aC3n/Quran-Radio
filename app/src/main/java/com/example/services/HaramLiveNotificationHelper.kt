package com.example.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.HaramSource
import com.example.model.QuranSurahsData

object HaramLiveNotificationHelper {

    const val CHANNEL_HARAM_LIVE_ID = "haram_live_recitation_channel"
    const val NOTIFICATION_HARAM_LIVE_ID = 3001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "تلاوات الحرمين الشريفين الحية 🕋"
            val descriptionText = "إشعار لطيف واختياري عند بدء تلاوة سورة في البث المباشر للحرم المكي أو المسجد النبوي"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_HARAM_LIVE_ID, name, importance).apply {
                description = descriptionText
                enableVibration(false)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showHaramNotification(
        context: Context,
        surahNumber: Int,
        surahName: String? = null,
        source: HaramSource = HaramSource.MAKKAH,
        reciterName: String? = null
    ) {
        val isAllowed = kotlinx.coroutines.runBlocking {
            AdhanNotificationHelper.isNotificationAllowed(context, null)
        }
        if (!isAllowed) return

        createNotificationChannel(context)

        val surah = if (surahName != null) {
            QuranSurahsData.getSurahByName(surahName) ?: QuranSurahsData.getSurah(surahNumber)
        } else {
            QuranSurahsData.getSurah(surahNumber)
        }

        val finalSurahName = surah?.nameAr ?: (surahName ?: "القرآن الكريم")
        val stationId = source.defaultStationId

        val title = when (source) {
            HaramSource.MAKKAH -> "الآن يُقرأ في الحرم المكي الشريف 🕋"
            HaramSource.MADINAH -> "الآن يُقرأ في المسجد النبوي الشريف 🕌"
            HaramSource.BOTH -> "الآن يُقرأ في الحرمين الشريفين 🕋"
        }

        val subtext = when (source) {
            HaramSource.MAKKAH -> "بث مباشر من المسجد الحرام بمكة المكرمة"
            HaramSource.MADINAH -> "بث مباشر من المسجد النبوي الشريف بالمدينة المنورة"
            HaramSource.BOTH -> "بث مباشر من رحاب الحرمين الشريفين"
        }

        val shortContent = "سورة $finalSurahName"
        val bigText = buildString {
            append("سورة $finalSurahName\n")
            if (!reciterName.isNullOrBlank()) {
                append("بصوت: $reciterName\n")
            }
            append("$subtext • اضغط على «استمع الآن 🎧» للانتقال للبث المباشر فوراً.")
        }

        // Deep link to play station
        val playIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("quranradio://station/$stationId")
            putExtra("EXTRA_AUTO_PLAY_STATION_ID", stationId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val playPendingIntent = PendingIntent.getActivity(
            context,
            stationId.hashCode(),
            playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Disable feature action
        val disableIntent = Intent(context, HaramNotificationActionReceiver::class.java).apply {
            action = HaramNotificationActionReceiver.ACTION_DISABLE_HARAM_NOTIFICATIONS
        }

        val disablePendingIntent = PendingIntent.getBroadcast(
            context,
            2002,
            disableIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_HARAM_LIVE_ID)
            .setSmallIcon(R.drawable.ic_adhan_notification)
            .setContentTitle(title)
            .setContentText(shortContent)
            .setSubText(if (source == HaramSource.MAKKAH) "الحرم المكي 🕋" else "المسجد النبوي 🕌")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(title)
                    .bigText(bigText)
            )
            .setColor(0xFF0284C7.toInt())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setAutoCancel(true)
            .setContentIntent(playPendingIntent)
            .addAction(
                android.R.drawable.ic_media_play,
                "استمع الآن 🎧",
                playPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "إيقاف هذه التنبيهات 🔕",
                disablePendingIntent
            )
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_HARAM_LIVE_ID, notification)
    }
}
