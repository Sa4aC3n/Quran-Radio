package com.example.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import com.example.MainActivity
import com.example.QuranRadioApplication
import com.example.R
import com.example.model.RadioStation
import com.example.model.StationCategory
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Android Media3 MediaLibraryService for Quran Radio World.
 *
 * Provides full integration with Android System Media Controls and Android Auto
 * with a structured browse hierarchy for in-car dashboard radio exploration.
 */
@OptIn(UnstableApi::class)
class QuranMediaService : MediaLibraryService() {

    private var mediaLibrarySession: MediaLibrarySession? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isBackgroundPlayEnabledCached: Boolean = true

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()

        val app = application as? QuranRadioApplication
        serviceScope.launch {
            app?.userPreferencesRepository?.isBackgroundPlayEnabled?.collect { enabled ->
                isBackgroundPlayEnabledCached = enabled
            }
        }
        val playerManager = app?.playerManager
        val player = playerManager?.getPlayer()

        if (player != null && playerManager != null) {
            val forwardingPlayer = object : ForwardingPlayer(player) {
                override fun getAvailableCommands(): Player.Commands {
                    return super.getAvailableCommands().buildUpon()
                        .add(Player.COMMAND_PLAY_PAUSE)
                        .add(Player.COMMAND_STOP)
                        .add(Player.COMMAND_SEEK_TO_NEXT)
                        .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                        .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                        .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                        .add(Player.COMMAND_SET_MEDIA_ITEM)
                        .build()
                }

                override fun isCommandAvailable(command: @Player.Command Int): Boolean {
                    return when (command) {
                        Player.COMMAND_PLAY_PAUSE,
                        Player.COMMAND_STOP,
                        Player.COMMAND_SEEK_TO_NEXT,
                        Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                        Player.COMMAND_SEEK_TO_PREVIOUS,
                        Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
                        Player.COMMAND_SET_MEDIA_ITEM -> true
                        else -> super.isCommandAvailable(command)
                    }
                }

                override fun play() {
                    playerManager.resume()
                }

                override fun pause() {
                    playerManager.pause()
                }

                override fun stop() {
                    playerManager.stop()
                }

                override fun seekToNext() {
                    playerManager.playNext()
                }

                override fun seekToNextMediaItem() {
                    playerManager.playNext()
                }

                override fun seekToPrevious() {
                    playerManager.playPrevious()
                }

                override fun seekToPreviousMediaItem() {
                    playerManager.playPrevious()
                }

                override fun setMediaItem(mediaItem: MediaItem) {
                    val stationId = mediaItem.mediaId
                    val station = app?.radioRepository?.getStationById(stationId)
                        ?: com.example.data.DefaultStationsData.STATIONS.find { it.id == stationId }
                    if (station != null) {
                        playerManager.playStation(station)
                    } else {
                        super.setMediaItem(mediaItem)
                    }
                }

                override fun setMediaItem(mediaItem: MediaItem, startPositionMs: Long) {
                    setMediaItem(mediaItem)
                }

                override fun setMediaItem(mediaItem: MediaItem, resetPosition: Boolean) {
                    setMediaItem(mediaItem)
                }
            }

            val sessionActivityPendingIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val libraryCallback = object : MediaLibrarySession.Callback {
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    val sessionCommands = MediaLibrarySession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon().build()
                    val playerCommands = session.player.availableCommands.buildUpon()
                        .add(Player.COMMAND_PLAY_PAUSE)
                        .add(Player.COMMAND_STOP)
                        .add(Player.COMMAND_SEEK_TO_NEXT)
                        .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                        .add(Player.COMMAND_SET_MEDIA_ITEM)
                        .build()

                    return MediaLibrarySession.ConnectionResult.AcceptedResultBuilder(session)
                        .setAvailableSessionCommands(sessionCommands)
                        .setAvailablePlayerCommands(playerCommands)
                        .build()
                }

                override fun onGetLibraryRoot(
                    session: MediaLibrarySession,
                    browser: MediaSession.ControllerInfo,
                    params: LibraryParams?
                ): ListenableFuture<LibraryResult<MediaItem>> {
                    val rootMetadata = MediaMetadata.Builder()
                        .setTitle("إذاعات القرآن الكريم")
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .build()
                    val rootItem = MediaItem.Builder()
                        .setMediaId(ROOT_ID)
                        .setMediaMetadata(rootMetadata)
                        .build()
                    return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
                }

                override fun onGetChildren(
                    session: MediaLibrarySession,
                    browser: MediaSession.ControllerInfo,
                    parentId: String,
                    page: Int,
                    pageSize: Int,
                    params: LibraryParams?
                ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
                    val children = getBrowseChildren(parentId, app)
                    return Futures.immediateFuture(LibraryResult.ofItemList(children, params))
                }

                override fun onGetItem(
                    session: MediaLibrarySession,
                    browser: MediaSession.ControllerInfo,
                    mediaId: String
                ): ListenableFuture<LibraryResult<MediaItem>> {
                    val item = getMediaItemById(mediaId, app)
                    return if (item != null) {
                        Futures.immediateFuture(LibraryResult.ofItem(item, null))
                    } else {
                        Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
                    }
                }

                override fun onPlaybackResumption(
                    mediaSession: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
                    val currentMediaItem = mediaSession.player.currentMediaItem
                    return if (currentMediaItem != null) {
                        Futures.immediateFuture(
                            MediaSession.MediaItemsWithStartPosition(
                                listOf(currentMediaItem),
                                0,
                                C.TIME_UNSET
                            )
                        )
                    } else {
                        super.onPlaybackResumption(mediaSession, controller)
                    }
                }
            }

            mediaLibrarySession = MediaLibrarySession.Builder(this, forwardingPlayer, libraryCallback)
                .setSessionActivity(sessionActivityPendingIntent)
                .setId("QuranRadioMediaLibrarySession")
                .build()

            playerManager.mediaSession = mediaLibrarySession

            // Configure Notification Provider with clear Channel and App Branding
            val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
                .setNotificationId(MEDIA_NOTIFICATION_ID)
                .setChannelId(MEDIA_CHANNEL_ID)
                .setChannelName(R.string.app_name)
                .build().apply {
                    setSmallIcon(R.drawable.ic_adhan_notification)
                }

            setMediaNotificationProvider(notificationProvider)

            // Immediately promote to foreground with genuine MediaStyle notification
            onUpdateNotification(mediaLibrarySession!!, true)

            // Observe player state changes to keep Media Player Control Card perfectly in sync
            serviceScope.launch {
                playerManager.playerState.collect {
                    mediaLibrarySession?.let { session ->
                        onUpdateNotification(session, false)
                    }
                }
            }
        } else {
            promoteToForegroundFallback()
        }
    }

    private fun getBrowseChildren(parentId: String, app: QuranRadioApplication?): ImmutableList<MediaItem> {
        val list = mutableListOf<MediaItem>()

        when (parentId) {
            ROOT_ID -> {
                list.add(createCategoryItem(CAT_FEATURED, "🌟 إذاعات مميزة مختارة"))
                list.add(createCategoryItem(CAT_HARAMAIN, "🕋 تلاوات الحرمين الشريفين"))
                list.add(createCategoryItem(CAT_CAIRO, "🇪🇬 إذاعة القرآن من القاهرة"))
                list.add(createCategoryItem(CAT_RECITERS, "🎙️ كبار مشاهير القراء"))
                list.add(createCategoryItem(CAT_ALL, "📻 جميع إذاعات القرآن الكريم"))
            }
            CAT_FEATURED -> {
                val featured = com.example.data.DefaultStationsData.STATIONS.filter { it.isFeatured }
                featured.forEach { list.add(stationToMediaItem(it)) }
            }
            CAT_HARAMAIN -> {
                val haramain = com.example.data.DefaultStationsData.STATIONS.filter {
                    it.id == "haram_makkah_live" || it.id == "haram_madinah_live"
                }
                haramain.forEach { list.add(stationToMediaItem(it)) }
            }
            CAT_CAIRO -> {
                val cairo = com.example.data.DefaultStationsData.STATIONS.filter {
                    it.id == "cairo_quran" || it.countryCode == "EG"
                }
                cairo.forEach { list.add(stationToMediaItem(it)) }
            }
            CAT_RECITERS -> {
                val reciters = com.example.data.DefaultStationsData.STATIONS.filter {
                    it.category == StationCategory.RECITER || it.reciter != null
                }.take(30)
                reciters.forEach { list.add(stationToMediaItem(it)) }
            }
            CAT_ALL -> {
                com.example.data.DefaultStationsData.STATIONS.take(50).forEach {
                    list.add(stationToMediaItem(it))
                }
            }
        }
        return ImmutableList.copyOf(list)
    }

    private fun createCategoryItem(id: String, title: String): MediaItem {
        return MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .build()
            )
            .build()
    }

    private fun stationToMediaItem(station: RadioStation): MediaItem {
        val artworkUri = if (station.logoUrl.isNotBlank()) Uri.parse(station.logoUrl) else null
        return MediaItem.Builder()
            .setMediaId(station.id)
            .setUri(Uri.parse(station.streamUrl))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(station.name)
                    .setArtist(station.reciter ?: station.country)
                    .setSubtitle(station.reciter ?: station.country)
                    .setArtworkUri(artworkUri)
                    .setIsPlayable(true)
                    .setIsBrowsable(false)
                    .build()
            )
            .build()
    }

    private fun getMediaItemById(mediaId: String, app: QuranRadioApplication?): MediaItem? {
        val station = app?.radioRepository?.getStationById(mediaId)
            ?: com.example.data.DefaultStationsData.STATIONS.find { it.id == mediaId }
        return station?.let { stationToMediaItem(it) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as? QuranRadioApplication
        val playerManager = app?.playerManager

        when (intent?.action) {
            ACTION_PLAY_PAUSE -> playerManager?.togglePlayPause()
            ACTION_PLAY -> playerManager?.resume()
            ACTION_PAUSE -> playerManager?.pause()
            ACTION_NEXT -> playerManager?.playNext()
            ACTION_PREVIOUS -> playerManager?.playPrevious()
            ACTION_STOP -> {
                playerManager?.stop()
                try {
                    ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                    val notificationManager = getSystemService(NotificationManager::class.java)
                    notificationManager?.cancel(MEDIA_NOTIFICATION_ID)
                } catch (_: Exception) {}
                stopSelf()
                return START_NOT_STICKY
            }
        }

        mediaLibrarySession?.let { session ->
            val player = session.player
            val isPlaying = player.playWhenReady && player.playbackState != Player.STATE_ENDED && player.playbackState != Player.STATE_IDLE
            onUpdateNotification(session, isPlaying)
        } ?: run {
            promoteToForegroundFallback()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        try {
            val notification = buildMediaNotification(session)
            val player = session.player
            val isPlaying = player.playWhenReady && player.playbackState != Player.STATE_ENDED && player.playbackState != Player.STATE_IDLE

            if (startInForegroundRequired || isPlaying) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ServiceCompat.startForeground(
                        this,
                        MEDIA_NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {
                    ServiceCompat.startForeground(
                        this,
                        MEDIA_NOTIFICATION_ID,
                        notification,
                        0
                    )
                }
            } else {
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH)
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager?.notify(MEDIA_NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e("QuranMediaService", "Failed onUpdateNotification: ${e.message}", e)
        }
    }

    private fun createActionPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, QuranMediaService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun buildMediaNotification(session: MediaSession): Notification {
        val player = session.player
        val isPlaying = player.playWhenReady && player.playbackState != Player.STATE_ENDED && player.playbackState != Player.STATE_IDLE

        val app = application as? QuranRadioApplication
        val currentStation = app?.playerManager?.playerState?.value?.currentStation
        val stationTitle = currentStation?.name ?: getString(R.string.app_name)
        val stationSubtitle = currentStation?.reciter ?: currentStation?.country ?: "إذاعة القرآن الكريم - بث مباشر"

        val prevPendingIntent = createActionPendingIntent(ACTION_PREVIOUS, 101)
        val togglePendingIntent = createActionPendingIntent(ACTION_PLAY_PAUSE, 102)
        val nextPendingIntent = createActionPendingIntent(ACTION_NEXT, 103)
        val stopPendingIntent = createActionPendingIntent(ACTION_STOP, 104)

        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val mediaStyle = MediaStyleNotificationHelper.MediaStyle(session)
            .setShowActionsInCompactView(0, 1, 2)
            .setShowCancelButton(true)
            .setCancelButtonIntent(stopPendingIntent)

        val playPauseIcon = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        val playPauseText = if (isPlaying) "إيقاف مؤقت" else "تشغيل"

        return NotificationCompat.Builder(this, MEDIA_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_adhan_notification)
            .setContentTitle(stationTitle)
            .setContentText(stationSubtitle)
            .setSubText(getString(R.string.app_name))
            .setContentIntent(sessionActivityPendingIntent)
            .setDeleteIntent(stopPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setOngoing(isPlaying)
            .setStyle(mediaStyle)
            .addAction(android.R.drawable.ic_media_previous, "السابق", prevPendingIntent)
            .addAction(playPauseIcon, playPauseText, togglePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "التالي", nextPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "إغلاق", stopPendingIntent)
            .build()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java) ?: return
            if (notificationManager.getNotificationChannel(MEDIA_CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    MEDIA_CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "إذاعة القرآن الكريم - التشغيل في الخلفية"
                    setShowBadge(false)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private fun promoteToForegroundFallback() {
        try {
            ensureNotificationChannel()
            val sessionActivityPendingIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(this, MEDIA_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_adhan_notification)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("بث مباشر لإذاعة القرآن الكريم")
                .setContentIntent(sessionActivityPendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    MEDIA_NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                ServiceCompat.startForeground(
                    this,
                    MEDIA_NOTIFICATION_ID,
                    notification,
                    0
                )
            }
        } catch (e: Exception) {
            Log.e("QuranMediaService", "Failed to promote fallback to foreground: ${e.message}", e)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val app = application as? QuranRadioApplication
        val isBgEnabled = isBackgroundPlayEnabledCached
        val player = mediaLibrarySession?.player
        val isPlaying = player != null && player.playWhenReady && player.playbackState != Player.STATE_ENDED && player.playbackState != Player.STATE_IDLE

        if (!isBgEnabled || !isPlaying) {
            app?.playerManager?.stop()
            try {
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager?.cancel(MEDIA_NOTIFICATION_ID)
            } catch (_: Exception) {}
            stopSelf()
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        try {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.cancel(MEDIA_NOTIFICATION_ID)
        } catch (_: Exception) {}
        val app = application as? QuranRadioApplication
        app?.playerManager?.mediaSession = null
        mediaLibrarySession?.run {
            release()
            mediaLibrarySession = null
        }
        super.onDestroy()
    }

    companion object {
        const val MEDIA_NOTIFICATION_ID = 1001
        const val MEDIA_CHANNEL_ID = "quran_radio_media_channel"

        const val ACTION_PLAY_PAUSE = "com.example.player.ACTION_PLAY_PAUSE"
        const val ACTION_PLAY = "com.example.player.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.player.ACTION_PAUSE"
        const val ACTION_NEXT = "com.example.player.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.player.ACTION_PREVIOUS"
        const val ACTION_STOP = "com.example.player.ACTION_STOP"

        const val ROOT_ID = "[ROOT]"
        const val CAT_FEATURED = "[CAT_FEATURED]"
        const val CAT_HARAMAIN = "[CAT_HARAMAIN]"
        const val CAT_CAIRO = "[CAT_CAIRO]"
        const val CAT_RECITERS = "[CAT_RECITERS]"
        const val CAT_ALL = "[CAT_ALL]"
    }
}
