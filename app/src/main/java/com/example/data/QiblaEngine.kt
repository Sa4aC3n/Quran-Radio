package com.example.data

import com.example.model.QiblaInfo
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Astronomical Great-Circle Qibla Calculation Engine.
 * Calculates exact bearing to the Holy Kaaba in Makkah (21.422487° N, 39.826206° E)
 * and distance using spherical trigonometry.
 */
object QiblaEngine {

    const val MECCA_LAT = 21.422487
    const val MECCA_LNG = 39.826206
    private const val EARTH_RADIUS_KM = 6371.0
    private const val RAD = Math.PI / 180.0
    private const val DEG = 180.0 / Math.PI

    /**
     * Calculate True Bearing to Mecca from user's coordinates in degrees (0..360).
     */
    fun calculateQiblaBearing(latitude: Double, longitude: Double): Float {
        val userLatRad = latitude * RAD
        val userLngRad = longitude * RAD
        val meccaLatRad = MECCA_LAT * RAD
        val meccaLngRad = MECCA_LNG * RAD

        val deltaLng = meccaLngRad - userLngRad

        val y = sin(deltaLng) * cos(meccaLatRad)
        val x = cos(userLatRad) * sin(meccaLatRad) - sin(userLatRad) * cos(meccaLatRad) * cos(deltaLng)

        var bearing = (atan2(y, x) * DEG).toFloat()
        if (bearing < 0) {
            bearing += 360f
        }
        return bearing
    }

    /**
     * Calculate distance to Holy Kaaba in kilometers using Haversine formula.
     */
    fun calculateDistanceToMecca(latitude: Double, longitude: Double): Double {
        val lat1 = latitude * RAD
        val lon1 = longitude * RAD
        val lat2 = MECCA_LAT * RAD
        val lon2 = MECCA_LNG * RAD

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2.0) * sin(dLat / 2.0) +
                cos(lat1) * cos(lat2) * sin(dLon / 2.0) * sin(dLon / 2.0)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return EARTH_RADIUS_KM * c
    }

    /**
     * Build complete QiblaInfo object for given coordinates and current sensor azimuth.
     */
    fun buildQiblaInfo(
        latitude: Double,
        longitude: Double,
        azimuth: Float,
        accuracy: Int = 3
    ): QiblaInfo {
        val bearing = calculateQiblaBearing(latitude, longitude)
        val distance = calculateDistanceToMecca(latitude, longitude)

        // relative angle between device orientation and Qibla (-180..180)
        var diff = bearing - azimuth
        while (diff > 180f) diff -= 360f
        while (diff < -180f) diff += 360f

        val isAligned = kotlin.math.abs(diff) <= 3.0f

        return QiblaInfo(
            userLatitude = latitude,
            userLongitude = longitude,
            qiblaBearing = bearing,
            distanceKm = distance,
            currentAzimuth = azimuth,
            relativeAngle = diff,
            isAligned = isAligned,
            accuracy = accuracy,
            isSensorAvailable = true
        )
    }
}
