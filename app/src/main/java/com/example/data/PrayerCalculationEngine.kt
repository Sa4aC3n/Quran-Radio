package com.example.data

import com.example.model.AsrJuristic
import com.example.model.CalculationMethod
import com.example.model.DailyPrayerSchedule
import com.example.model.PrayerTimeItem
import com.example.model.PrayerType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/**
 * Astronomical Prayer Time Calculation Engine.
 * 100% offline, precise solar calculations based on standard astronomical models.
 */
object PrayerCalculationEngine {

    private const val RAD = Math.PI / 180.0
    private const val DEG = 180.0 / Math.PI

    /**
     * Compute full daily schedule for a specified date and geographic coordinates.
     */
    fun calculatePrayerTimes(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        timeZone: TimeZone = calendar.timeZone,
        method: CalculationMethod = CalculationMethod.EGYPTIAN,
        asrJuristic: AsrJuristic = AsrJuristic.STANDARD,
        minuteOffsets: Map<PrayerType, Int> = emptyMap(),
        cityName: String = "القاهرة",
        countryName: String = "مصر"
    ): DailyPrayerSchedule {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val rawTimes = computeRawPrayerTimes(
            year = year,
            month = month,
            day = day,
            latitude = latitude,
            longitude = longitude,
            timeZoneOffset = timeZone.getOffset(calendar.timeInMillis) / 3600000.0,
            method = method,
            asrJuristic = asrJuristic
        )

        val baseCal = Calendar.getInstance(timeZone).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        fun createPrayerItem(type: PrayerType, hourFraction: Double): PrayerTimeItem {
            val offsetMin = minuteOffsets[type] ?: 0
            val totalMinutes = (hourFraction * 60.0).toInt() + offsetMin
            val hour = (totalMinutes / 60) % 24
            val min = totalMinutes % 60

            val itemCal = (baseCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, min)
            }
            val timeMillis = itemCal.timeInMillis

            val format12 = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timeMillis))
            val format24 = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timeMillis))

            return PrayerTimeItem(
                type = type,
                timeInMillis = timeMillis,
                formattedTime12h = format12,
                formattedTime24h = format24
            )
        }

        val fajr = createPrayerItem(PrayerType.FAJR, rawTimes[0])
        val sunrise = createPrayerItem(PrayerType.SUNRISE, rawTimes[1])
        val dhuhr = createPrayerItem(PrayerType.DHUHR, rawTimes[2])
        val asr = createPrayerItem(PrayerType.ASR, rawTimes[3])
        val maghrib = createPrayerItem(PrayerType.MAGHRIB, rawTimes[4])
        val isha = createPrayerItem(PrayerType.ISHA, rawTimes[5])

        val all = listOf(fajr, sunrise, dhuhr, asr, maghrib, isha)
        val now = System.currentTimeMillis()

        // Find current and next prayer
        var nextItem = all.firstOrNull { it.timeInMillis > now }
        var currentItem: PrayerTimeItem? = null

        if (nextItem == null) {
            // All prayers for today have passed; next prayer is tomorrow's Fajr
            val tomorrowCal = (calendar.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
            val tomorrowTimes = calculatePrayerTimes(
                calendar = tomorrowCal,
                latitude = latitude,
                longitude = longitude,
                timeZone = timeZone,
                method = method,
                asrJuristic = asrJuristic,
                minuteOffsets = minuteOffsets,
                cityName = cityName,
                countryName = countryName
            )
            nextItem = tomorrowTimes.fajr
            currentItem = isha
        } else {
            val nextIndex = all.indexOf(nextItem)
            currentItem = if (nextIndex > 0) all[nextIndex - 1] else null
        }

        val updatedPrayers = all.map {
            it.copy(
                isCurrent = it.type == currentItem?.type,
                isNext = it.type == nextItem.type
            )
        }

        val finalFajr = updatedPrayers[0]
        val finalSunrise = updatedPrayers[1]
        val finalDhuhr = updatedPrayers[2]
        val finalAsr = updatedPrayers[3]
        val finalMaghrib = updatedPrayers[4]
        val finalIsha = updatedPrayers[5]

        val millisUntilNext = (nextItem.timeInMillis - now).coerceAtLeast(0L)

        // Calculate progress percentage between previous prayer and next prayer
        val prevMillis = currentItem?.timeInMillis ?: (nextItem.timeInMillis - 4 * 3600000L)
        val totalSpan = (nextItem.timeInMillis - prevMillis).coerceAtLeast(1L)
        val elapsed = (now - prevMillis).coerceIn(0L, totalSpan)
        val progress = (elapsed.toFloat() / totalSpan.toFloat()).coerceIn(0f, 1f)

        val gregorianFormatted = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("ar")).format(calendar.time)
        val hijriFormatted = getHijriDateFormatted(calendar)

        return DailyPrayerSchedule(
            dateInMillis = calendar.timeInMillis,
            gregorianDateFormatted = gregorianFormatted,
            hijriDateFormatted = hijriFormatted,
            cityName = cityName,
            countryName = countryName,
            latitude = latitude,
            longitude = longitude,
            calculationMethod = method,
            asrJuristic = asrJuristic,
            fajr = finalFajr,
            sunrise = finalSunrise,
            dhuhr = finalDhuhr,
            asr = finalAsr,
            maghrib = finalMaghrib,
            isha = finalIsha,
            nextPrayer = nextItem,
            millisUntilNextPrayer = millisUntilNext,
            progressToNextPrayer = progress
        )
    }

    /**
     * Compute raw hour fractions (0.0 .. 24.0) for the 6 prayer milestones.
     */
    private fun computeRawPrayerTimes(
        year: Int,
        month: Int,
        day: Int,
        latitude: Double,
        longitude: Double,
        timeZoneOffset: Double,
        method: CalculationMethod,
        asrJuristic: AsrJuristic
    ): DoubleArray {
        val jd = julianDate(year, month, day)
        val d = jd - 2451545.0

        // Mean solar anomaly and true longitude
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(g * RAD) + 0.020 * sin(2.0 * g * RAD))

        // Obliquity of ecliptic
        val e = 23.439 - 0.00000036 * d

        // Declination and Equation of Time
        val declination = asin(sin(e * RAD) * sin(l * RAD)) * DEG
        val ra = fixHour(atan2(cos(e * RAD) * sin(l * RAD), cos(l * RAD)) * DEG / 15.0)
        val eqTime = q / 15.0 - ra

        // Solar Noon (Dhuhr)
        val dhuhr = fixHour(12.0 + timeZoneOffset - longitude / 15.0 - eqTime)

        // Sunrise and Sunset (standard angle = 0.833)
        val sunRiseAngle = 0.833
        val sunriseHourAngle = hourAngle(latitude, declination, sunRiseAngle)
        val sunrise = dhuhr - sunriseHourAngle
        val sunset = dhuhr + sunriseHourAngle

        // Fajr
        val fajrHourAngle = hourAngle(latitude, declination, method.fajrAngle)
        val fajr = dhuhr - fajrHourAngle

        // Asr
        val shadowRatio = if (asrJuristic == AsrJuristic.HANAFI) 2.0 else 1.0
        val asrAngle = -atan(1.0 / (shadowRatio + tan(abs(latitude - declination) * RAD))) * DEG
        val asrHourAngle = hourAngle(latitude, declination, asrAngle)
        val asr = dhuhr + asrHourAngle

        // Maghrib
        val maghrib = sunset

        // Isha
        val isha = if (method.ishaIntervalMinutes > 0) {
            maghrib + (method.ishaIntervalMinutes / 60.0)
        } else {
            val ishaHourAngle = hourAngle(latitude, declination, method.ishaAngle)
            dhuhr + ishaHourAngle
        }

        return doubleArrayOf(fajr, sunrise, dhuhr, asr, maghrib, isha)
    }

    private fun hourAngle(lat: Double, dec: Double, angle: Double): Double {
        val cosHa = (-sin(angle * RAD) - sin(lat * RAD) * sin(dec * RAD)) /
                (cos(lat * RAD) * cos(dec * RAD))
        return if (cosHa > 1.0) 0.0 else if (cosHa < -1.0) 12.0 else acos(cosHa) * DEG / 15.0
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle - 360.0 * floor(angle / 360.0)
        if (a < 0) a += 360.0
        return a
    }

    private fun fixHour(hour: Double): Double {
        var h = hour - 24.0 * floor(hour / 24.0)
        if (h < 0) h += 24.0
        return h
    }

    /**
     * Compute approximate Islamic Hijri Date based on astronomical Umm Al-Qura calculation.
     */
    fun getHijriDateFormatted(calendar: Calendar): String {
        val gYear = calendar.get(Calendar.YEAR)
        val gMonth = calendar.get(Calendar.MONTH) + 1
        val gDay = calendar.get(Calendar.DAY_OF_MONTH)

        val jd = julianDate(gYear, gMonth, gDay).toInt()
        val l = jd - 1948440 + 10632
        val n = ((l - 1) / 10631).toInt()
        val l2 = l - 10631 * n + 354
        val j = (((10985 - l2) / 5316).toInt()) * (((50 * l2) / 17719).toInt()) +
                ((l2 / 5670).toInt()) * (((43 * l2) / 15238).toInt())
        val l3 = l2 - (((30 - j) / 15).toInt()) * (((17719 * j) / 50).toInt()) -
                ((j / 16).toInt()) * (((15238 * j) / 43).toInt()) + 29
        val hMonth = ((24 * l3) / 709).toInt()
        val hDay = l3 - ((709 * hMonth) / 24).toInt()
        val hYear = (30 * n + j - 30).toInt()

        val hijriMonthsAr = arrayOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
            "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
        )
        val monthName = hijriMonthsAr.getOrElse(hMonth - 1) { "رمضان" }
        return "$hDay $monthName $hYear هـ"
    }
}
