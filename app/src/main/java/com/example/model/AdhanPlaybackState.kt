package com.example.model

/**
 * Lifecycle states of the Adhan playback system.
 */
enum class AdhanPlaybackState {
    IDLE,
    SCHEDULED,
    STARTING,
    PLAYING,
    STOPPING,
    COMPLETED,
    FAILED
}
