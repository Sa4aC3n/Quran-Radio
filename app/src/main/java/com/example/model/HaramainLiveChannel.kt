package com.example.model

enum class HaramainChannelId(val id: String) {
    MAKKAH("makkah"),
    MADINAH("madinah")
}

data class HaramainLiveChannel(
    val id: HaramainChannelId,
    val nameAr: String,
    val subtitleAr: String,
    val sourcePageUrl: String,
    val primaryStreamUrl: String,
    val fallbackStreamUrls: List<String> = emptyList(),
    val streamType: String = "HLS",
    val isVerified: Boolean = true,
    val description: String = ""
) {
    fun getAllStreamUrls(): List<String> {
        val list = mutableListOf<String>()
        if (primaryStreamUrl.isNotBlank()) list.add(primaryStreamUrl)
        list.addAll(fallbackStreamUrls.filter { it.isNotBlank() && it != primaryStreamUrl })
        return list
    }
}

enum class HaramainPlayerUiState {
    IDLE,
    CONNECTING,
    PLAYING,
    BUFFERING,
    PAUSED,
    ERROR
}
