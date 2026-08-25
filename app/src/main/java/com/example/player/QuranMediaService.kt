package com.example.player

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.MainActivity
import com.example.QuranRadioApplication

@OptIn(UnstableApi::class)
class QuranMediaService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val app = application as QuranRadioApplication
        val playerManager = app.playerManager
        val player = playerManager.getPlayer()

        if (player != null) {
            val sessionActivityPendingIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            mediaSession = MediaSession.Builder(this, player)
                .setSessionActivity(sessionActivityPendingIntent)
                .setId("QuranRadioMediaSession")
                .build()

            playerManager.mediaSession = mediaSession
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        val app = application as? QuranRadioApplication
        app?.playerManager?.mediaSession = null
        mediaSession?.run {
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
