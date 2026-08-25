package com.example.model

data class StationStream(
    val id: String,
    val stationId: String,
    val url: String,
    val format: String = "MP3",
    val priority: Int = 1,
    val isPrimary: Boolean = true,
    val isBackup: Boolean = false,
    val isActive: Boolean = true,
    val status: String = "ONLINE", // ONLINE, OFFLINE, UNKNOWN, PROTECTED
    val lastCheckedAt: Long = System.currentTimeMillis(),
    val lastSuccessAt: Long = System.currentTimeMillis(),
    val failureCount: Int = 0,
    val healthScore: Int = 100
)
