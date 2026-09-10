package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.data.QiblaEngine
import com.example.model.CityLocation
import com.example.model.PresetCities
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume

class AppLocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResult? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            return@withContext null
        }

        try {
            // Try FusedLocationProviderClient first
            val location = suspendCancellableCoroutine<Location?> { cont ->
                val cts = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            cont.resume(loc)
                        } else {
                            // Try last known location
                            fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                                cont.resume(lastLoc)
                            }.addOnFailureListener {
                                cont.resume(null)
                            }
                        }
                    }
                    .addOnFailureListener {
                        cont.resume(null)
                    }

                cont.invokeOnCancellation {
                    cts.cancel()
                }
            }

            val finalLocation = location ?: getSystemLocationFallback()
            if (finalLocation != null) {
                val geocoded = reverseGeocode(finalLocation.latitude, finalLocation.longitude)
                return@withContext LocationResult(
                    latitude = finalLocation.latitude,
                    longitude = finalLocation.longitude,
                    cityName = geocoded.first,
                    countryName = geocoded.second,
                    countryCode = geocoded.third,
                    timeZoneId = TimeZone.getDefault().id
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext null
    }

    @SuppressLint("MissingPermission")
    private fun getSystemLocationFallback(): Location? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        for (provider in providers) {
            if (lm.isProviderEnabled(provider)) {
                val loc = lm.getLastKnownLocation(provider)
                if (loc != null) return loc
            }
        }
        return null
    }

    suspend fun reverseGeocode(lat: Double, lng: Double): Triple<String, String, String> = withContext(Dispatchers.IO) {
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale("ar"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val addresses = suspendCancellableCoroutine<List<Address>> { cont ->
                        geocoder.getFromLocation(lat, lng, 1) { addresses ->
                            cont.resume(addresses ?: emptyList())
                        }
                    }
                    if (addresses.isNotEmpty()) {
                        val addr = addresses[0]
                        val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "موقعي الحالي"
                        val country = addr.countryName ?: "البلد"
                        val countryCode = addr.countryCode ?: "EG"
                        return@withContext Triple(city, country, countryCode)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val list = geocoder.getFromLocation(lat, lng, 1)
                    if (!list.isNullOrEmpty()) {
                        val addr = list[0]
                        val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "موقعي الحالي"
                        val country = addr.countryName ?: "البلد"
                        val countryCode = addr.countryCode ?: "EG"
                        return@withContext Triple(city, country, countryCode)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Offline Fallback: find nearest preset city
        val nearest = PresetCities.list.minByOrNull { city ->
            val dLat = city.latitude - lat
            val dLng = city.longitude - lng
            dLat * dLat + dLng * dLng
        }

        if (nearest != null) {
            return@withContext Triple(nearest.nameAr, nearest.countryAr, nearest.countryCode)
        }

        return@withContext Triple("القاهرة", "مصر", "EG")
    }

    data class LocationResult(
        val latitude: Double,
        val longitude: Double,
        val cityName: String,
        val countryName: String,
        val countryCode: String,
        val timeZoneId: String
    )
}
