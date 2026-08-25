package com.example.model

data class Reciter(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val styleAr: String = "مرتل",
    val country: String = "مصر",
    val stationId: String? = null,
    val hasLiveStream: Boolean = true,
    val avatarUrl: String = "",
    val bioAr: String = ""
)
