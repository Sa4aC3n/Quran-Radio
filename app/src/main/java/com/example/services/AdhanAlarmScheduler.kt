package com.example.services

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.MainActivity
import com.example.data.PrayerCalculationEngine
import com.example.data.PrayerPreferencesRepository
import com.example.model.PrayerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

object AdhanAlarmScheduler {

    const val ACTION_PRAYER_ALARM = "com.example.quranradio.ACTION_PRAYER_ALARM"
    const val ACTION_IQAMAH_ALARM = "com.example.quranradio.ACTION_IQAMAH_ALARM"

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleUpcomingAlarms(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = PrayerPreferencesRepository(context)
                val lat = prefs.latitude.first()
                val lng = prefs.longitude.first()
                val cityName = prefs.cityName.first()
                val countryName = prefs.countryName.first()
                val tzId = prefs.timeZoneId.first()
                val method = prefs.calculationMethod.first()
                val asrJuristic = prefs.asrJuristic.first()
                val isMasterEnabled = prefs.isAdhanMasterEnabled.first()
                val alertSettings = prefs.alertSettings.first()
                val iqamahSettings = prefs.iqamahAlertSettings.first()
                val iqamahDelays = prefs.iqamahDelays.first()
                val offsets = prefs.minuteOffsets.first()

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val timeZone = TimeZone.getTimeZone(tzId)
                val now = System.currentTimeMillis()

                if (!isMasterEnabled) {
                    android.util.Log.d("AdhanScheduler", "[ADHAN] Master Adhan toggle is disabled. Alarms skipped.")
                    return@launch
                }

                // Schedule for next 7 days
                for (dayOffset in 0..6) {
                    val cal = Calendar.getInstance(timeZone).apply {
                        add(Calendar.DAY_OF_YEAR, dayOffset)
                    }

                    val schedule = PrayerCalculationEngine.calculatePrayerTimes(
                        calendar = cal,
                        latitude = lat,
                        longitude = lng,
                        timeZone = timeZone,
                        method = method,
                        asrJuristic = asrJuristic,
                        minuteOffsets = offsets,
                        cityName = cityName,
                        countryName = countryName
                    )

                    for (item in schedule.allPrayers) {
                        // Skip sunrise (not a fardh prayer for adhan) unless user explicitly turned it on
                        if (item.type == PrayerType.SUNRISE && alertSettings[PrayerType.SUNRISE] != true) {
                            continue
                        }

                        val isEnabled = alertSettings[item.type] ?: true
                        val prayerTimeMillis = item.timeInMillis

                        if (isEnabled && prayerTimeMillis > now) {
                            val requestCode = generateRequestCode(dayOffset, item.type.ordinal, isIqamah = false)

                            val intent = Intent(context, AdhanAlarmReceiver::class.java).apply {
                                action = ACTION_PRAYER_ALARM
                                putExtra("PRAYER_TYPE_ID", item.type.id)
                                putExtra("CITY_NAME", cityName)
                                putExtra("SCHEDULED_TIME", prayerTimeMillis)
                            }

                            val pendingIntent = PendingIntent.getBroadcast(
                                context,
                                requestCode,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )

                            val showIntent = Intent(context, MainActivity::class.java)
                            val showPendingIntent = PendingIntent.getActivity(
                                context,
                                requestCode + 5000,
                                showIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                val alarmClockInfo = AlarmManager.AlarmClockInfo(prayerTimeMillis, showPendingIntent)
                                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                            } else {
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, prayerTimeMillis, pendingIntent)
                            }
                            android.util.Log.d("AdhanScheduler", "[ADHAN] Prayer scheduled: ${item.type.arabicName} at ${item.formattedTime12h}")
                        }

                        // Schedule Iqamah Alarm if enabled
                        val isIqamahEnabled = iqamahSettings[item.type] ?: false
                        val delayMin = iqamahDelays[item.type] ?: 15
                        val iqamahTimeMillis = prayerTimeMillis + (delayMin * 60 * 1000L)

                        if (isIqamahEnabled && iqamahTimeMillis > now && item.type != PrayerType.SUNRISE) {
                            val iqamahReqCode = generateRequestCode(dayOffset, item.type.ordinal, isIqamah = true)
                            val iqamahIntent = Intent(context, IqamahAlarmReceiver::class.java).apply {
                                action = ACTION_IQAMAH_ALARM
                                putExtra("PRAYER_TYPE_ID", item.type.id)
                                putExtra("CITY_NAME", cityName)
                            }
                            val iqamahPendingIntent = PendingIntent.getBroadcast(
                                context,
                                iqamahReqCode,
                                iqamahIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                alarmManager.setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    iqamahTimeMillis,
                                    iqamahPendingIntent
                                )
                            } else {
                                alarmManager.setExact(
                                    AlarmManager.RTC_WAKEUP,
                                    iqamahTimeMillis,
                                    iqamahPendingIntent
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun generateRequestCode(dayOffset: Int, prayerIndex: Int, isIqamah: Boolean): Int {
        val base = dayOffset * 100 + prayerIndex * 10
        return if (isIqamah) base + 1 else base
    }
}
