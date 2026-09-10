package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.example.data.HaramPreferencesRepository
import com.example.data.HaramainStreamRepository
import com.example.data.ListeningStatsRepository
import com.example.data.PrayerPreferencesRepository
import com.example.data.RadioRepository
import com.example.data.UserPreferencesRepository
import com.example.location.AppLocationManager
import com.example.player.PlayerManager
import com.example.sensor.QiblaSensorManager
import com.example.services.AdhanAlarmScheduler
import com.example.services.AdhanAudioPlayer
import com.example.services.AdhanNotificationHelper
import com.example.services.HaramLiveNotificationHelper

class QuranRadioApplication : Application(), ImageLoaderFactory {

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    lateinit var prayerPreferencesRepository: PrayerPreferencesRepository
        private set

    lateinit var haramPreferencesRepository: HaramPreferencesRepository
        private set

    lateinit var radioRepository: RadioRepository
        private set

    lateinit var listeningStatsRepository: ListeningStatsRepository
        private set

    lateinit var haramainStreamRepository: HaramainStreamRepository
        private set

    lateinit var playerManager: PlayerManager
        private set

    lateinit var appLocationManager: AppLocationManager
        private set

    lateinit var qiblaSensorManager: QiblaSensorManager
        private set

    lateinit var adhanAudioPlayer: AdhanAudioPlayer
        private set

    lateinit var adhanManager: com.example.services.AdhanManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        userPreferencesRepository = UserPreferencesRepository(this)
        prayerPreferencesRepository = PrayerPreferencesRepository(this)
        haramPreferencesRepository = HaramPreferencesRepository(this)
        listeningStatsRepository = ListeningStatsRepository(this)
        haramainStreamRepository = HaramainStreamRepository()
        radioRepository = RadioRepository(this)
        playerManager = PlayerManager.getInstance(this, userPreferencesRepository, radioRepository)
        appLocationManager = AppLocationManager(this)
        qiblaSensorManager = QiblaSensorManager(this)
        adhanAudioPlayer = AdhanAudioPlayer(this)
        adhanManager = com.example.services.AdhanManager.getInstance(this)

        AdhanNotificationHelper.createNotificationChannels(this)
        HaramLiveNotificationHelper.createNotificationChannel(this)
        AdhanAlarmScheduler.scheduleUpcomingAlarms(this)
        com.example.services.WorkManagerScheduler.setupPeriodicBackgroundTasks(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.20) // Limit in-memory bitmap cache to 20% of available app heap
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024) // 50 MB disk cache limit
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .respectCacheHeaders(false)
            .build()
    }

    companion object {
        lateinit var instance: QuranRadioApplication
            private set
    }
}
