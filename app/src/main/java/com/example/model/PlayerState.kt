package com.example.model

enum class PlaybackStatus {
    IDLE,
    CONNECTING,
    PLAYING,
    PAUSED,
    BUFFERING,
    ERROR,
    STOPPED
}

data class PlayerState(
    val status: PlaybackStatus = PlaybackStatus.IDLE,
    val currentStation: RadioStation? = null,
    val errorMessage: String? = null,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val isAutoRetrying: Boolean = false,
    val isUserStopped: Boolean = false,
    val sleepTimerRemainingSeconds: Long = 0L,
    val isSleepTimerActive: Boolean = false,
    val playbackDurationMs: Long = 0L
)
