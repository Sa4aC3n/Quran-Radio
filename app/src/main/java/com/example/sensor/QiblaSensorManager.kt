package com.example.sensor

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.QiblaEngine
import com.example.model.QiblaInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class QiblaSensorManager(context: Context) : SensorEventListener {

    private val appContext = context.applicationContext
    private val sensorManager: SensorManager? = appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    private var rotationVectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var magneticSensor: Sensor? = null

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var smoothedAzimuth = 0f
    private var currentLat = 30.0444
    private var currentLng = 31.2357
    private var wasAligned = false

    private val _qiblaState = MutableStateFlow(
        QiblaInfo(
            userLatitude = currentLat,
            userLongitude = currentLng,
            qiblaBearing = QiblaEngine.calculateQiblaBearing(currentLat, currentLng),
            distanceKm = QiblaEngine.calculateDistanceToMecca(currentLat, currentLng),
            currentAzimuth = 0f,
            relativeAngle = QiblaEngine.calculateQiblaBearing(currentLat, currentLng),
            isAligned = false,
            accuracy = 3,
            isSensorAvailable = true
        )
    )
    val qiblaState: StateFlow<QiblaInfo> = _qiblaState.asStateFlow()

    init {
        try {
            rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            if (rotationVectorSensor == null) {
                accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                magneticSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            }
        } catch (_: Exception) {}
    }

    fun updateCoordinates(latitude: Double, longitude: Double) {
        currentLat = latitude
        currentLng = longitude
        recalculateQibla()
    }

    fun startListening() {
        val sm = sensorManager
        if (sm == null) {
            _qiblaState.value = _qiblaState.value.copy(isSensorAvailable = false)
            return
        }
        val hasSensor = rotationVectorSensor != null || (accelerometerSensor != null && magneticSensor != null)
        if (!hasSensor) {
            _qiblaState.value = _qiblaState.value.copy(isSensorAvailable = false)
            return
        }

        try {
            if (rotationVectorSensor != null) {
                sm.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
            } else {
                accelerometerSensor?.let {
                    sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
                }
                magneticSensor?.let {
                    sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
                }
            }
        } catch (_: Exception) {}
    }

    fun stopListening() {
        try {
            sensorManager?.unregisterListener(this)
        } catch (_: Exception) {}
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        var rawAzimuth: Float? = null

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            rawAzimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
            if (computeOrientationMatrix()) {
                rawAzimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            }
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
            if (computeOrientationMatrix()) {
                rawAzimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            }
        }

        if (rawAzimuth != null) {
            if (rawAzimuth < 0) {
                rawAzimuth += 360f
            }

            // Magnetic declination correction to True North
            val geomagneticField = GeomagneticField(
                currentLat.toFloat(),
                currentLng.toFloat(),
                0f,
                System.currentTimeMillis()
            )
            val trueNorthAzimuth = (rawAzimuth + geomagneticField.declination + 360f) % 360f

            // Smooth using circular low-pass filter
            smoothedAzimuth = smoothAngle(smoothedAzimuth, trueNorthAzimuth, 0.15f)

            val info = QiblaEngine.buildQiblaInfo(
                latitude = currentLat,
                longitude = currentLng,
                azimuth = smoothedAzimuth,
                accuracy = event.accuracy
            )

            // Trigger vibration feedback on entering alignment zone
            if (info.isAligned && !wasAligned) {
                vibrateAlignment()
            }
            wasAligned = info.isAligned

            _qiblaState.value = info
        }
    }

    private fun computeOrientationMatrix(): Boolean {
        val success = SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        if (success) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            return true
        }
        return false
    }

    private fun smoothAngle(current: Float, target: Float, factor: Float): Float {
        var diff = target - current
        while (diff > 180f) diff -= 360f
        while (diff < -180f) diff += 360f
        val result = (current + diff * factor) % 360f
        return if (result < 0) result + 360f else result
    }

    private fun recalculateQibla() {
        val currentInfo = _qiblaState.value
        _qiblaState.value = QiblaEngine.buildQiblaInfo(
            latitude = currentLat,
            longitude = currentLng,
            azimuth = currentInfo.currentAzimuth,
            accuracy = currentInfo.accuracy
        )
    }

    private fun vibrateAlignment() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _qiblaState.value = _qiblaState.value.copy(accuracy = accuracy)
    }
}
