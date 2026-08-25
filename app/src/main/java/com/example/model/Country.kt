package com.example.model

data class Country(
    val code: String,
    val nameAr: String,
    val nameEn: String,
    val flagEmoji: String,
    val stationCount: Int = 0
)
