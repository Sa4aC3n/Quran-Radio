package com.example.model

import androidx.compose.runtime.Immutable

/**
 * Predefined city for location selection and offline prayer calculations.
 */
@Immutable
data class CityLocation(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val countryAr: String,
    val countryEn: String,
    val countryCode: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
    val recommendedMethod: CalculationMethod = CalculationMethod.forCountryCode(countryCode)
)

object PresetCities {
    val list: List<CityLocation> = listOf(
        // Egypt
        CityLocation("cairo", "القاهرة", "Cairo", "مصر", "Egypt", "EG", 30.0444, 31.2357, "Africa/Cairo"),
        CityLocation("alexandria", "الإسكندرية", "Alexandria", "مصر", "Egypt", "EG", 31.2001, 29.9187, "Africa/Cairo"),
        CityLocation("giza", "الجيزة", "Giza", "مصر", "Egypt", "EG", 30.0131, 31.2089, "Africa/Cairo"),
        CityLocation("luxor", "الأقصر", "Luxor", "مصر", "Egypt", "EG", 25.6872, 32.6396, "Africa/Cairo"),
        CityLocation("aswan", "أسوان", "Aswan", "مصر", "Egypt", "EG", 24.0889, 32.8998, "Africa/Cairo"),
        CityLocation("tanta", "طنطا", "Tanta", "مصر", "Egypt", "EG", 30.7865, 31.0004, "Africa/Cairo"),
        CityLocation("mansoura", "المنصورة", "Mansoura", "مصر", "Egypt", "EG", 31.0409, 31.3785, "Africa/Cairo"),
        CityLocation("port_said", "بورسعيد", "Port Said", "مصر", "Egypt", "EG", 31.2653, 32.3019, "Africa/Cairo"),
        CityLocation("suez", "السويس", "Suez", "مصر", "Egypt", "EG", 29.9668, 32.5498, "Africa/Cairo"),
        CityLocation("ismailia", "الإسماعيلية", "Ismailia", "مصر", "Egypt", "EG", 30.5965, 32.2715, "Africa/Cairo"),

        // Saudi Arabia
        CityLocation("makkah", "مكة المكرمة", "Makkah", "السعودية", "Saudi Arabia", "SA", 21.4225, 39.8262, "Asia/Riyadh"),
        CityLocation("madinah", "المدينة المنورة", "Madinah", "السعودية", "Saudi Arabia", "SA", 24.5247, 39.5692, "Asia/Riyadh"),
        CityLocation("riyadh", "الرياض", "Riyadh", "السعودية", "Saudi Arabia", "SA", 24.7136, 46.6753, "Asia/Riyadh"),
        CityLocation("jeddah", "جدة", "Jeddah", "السعودية", "Saudi Arabia", "SA", 21.5433, 39.1728, "Asia/Riyadh"),
        CityLocation("dammam", "الدمام", "Dammam", "السعودية", "Saudi Arabia", "SA", 26.4207, 50.0888, "Asia/Riyadh"),
        CityLocation("taif", "الطائف", "Taif", "السعودية", "Saudi Arabia", "SA", 21.2854, 40.4222, "Asia/Riyadh"),
        CityLocation("tabuk", "تبوك", "Tabuk", "السعودية", "Saudi Arabia", "SA", 28.3835, 36.5662, "Asia/Riyadh"),
        CityLocation("abha", "أبها", "Abha", "السعودية", "Saudi Arabia", "SA", 18.2164, 42.5053, "Asia/Riyadh"),

        // Palestine & Levant
        CityLocation("jerusalem", "القدس الشريف", "Jerusalem", "فلسطين", "Palestine", "PS", 31.7683, 35.2137, "Asia/Jerusalem"),
        CityLocation("gaza", "غزة", "Gaza", "فلسطين", "Palestine", "PS", 31.5017, 34.4668, "Asia/Gaza"),
        CityLocation("amman", "عمّان", "Amman", "الأردن", "Jordan", "JO", 31.9454, 35.9284, "Asia/Amman"),
        CityLocation("damascus", "دمشق", "Damascus", "سوريا", "Syria", "SY", 33.5138, 36.2765, "Asia/Damascus"),
        CityLocation("aleppo", "حلب", "Aleppo", "سوريا", "Syria", "SY", 36.2021, 37.1343, "Asia/Damascus"),
        CityLocation("beirut", "بيروت", "Beirut", "لبنان", "Lebanon", "LB", 33.8938, 35.5018, "Asia/Beirut"),

        // Gulf
        CityLocation("dubai", "دبي", "Dubai", "الإمارات", "UAE", "AE", 25.2048, 55.2708, "Asia/Dubai"),
        CityLocation("abu_dhabi", "أبوظبي", "Abu Dhabi", "الإمارات", "UAE", "AE", 24.4539, 54.3773, "Asia/Dubai"),
        CityLocation("sharjah", "الشارقة", "Sharjah", "الإمارات", "UAE", "AE", 25.3463, 55.4209, "Asia/Dubai"),
        CityLocation("kuwait_city", "مدينة الكويت", "Kuwait City", "الكويت", "Kuwait", "KW", 29.3759, 47.9774, "Asia/Kuwait"),
        CityLocation("doha", "الدوحة", "Doha", "قطر", "Qatar", "QA", 25.2854, 51.5310, "Asia/Qatar"),
        CityLocation("manama", "المنامة", "Manama", "البحرين", "Bahrain", "BH", 26.2285, 50.5860, "Asia/Bahrain"),
        CityLocation("muscat", "مسقط", "Muscat", "عُمان", "Oman", "OM", 23.5859, 58.4059, "Asia/Muscat"),
        CityLocation("salalah", "صلالة", "Salalah", "عُمان", "Oman", "OM", 17.0151, 54.0924, "Asia/Muscat"),

        // Iraq & Yemen
        CityLocation("baghdad", "بغداد", "Baghdad", "العراق", "Iraq", "IQ", 33.3152, 44.3661, "Asia/Baghdad"),
        CityLocation("basra", "البصرة", "Basra", "العراق", "Iraq", "IQ", 30.5081, 47.7835, "Asia/Baghdad"),
        CityLocation("erbil", "أربيل", "Erbil", "العراق", "Iraq", "IQ", 36.1911, 44.0092, "Asia/Baghdad"),
        CityLocation("sanaa", "صنعاء", "Sanaa", "اليمن", "Yemen", "YE", 15.3694, 44.1910, "Asia/Aden"),
        CityLocation("aden", "عدن", "Aden", "اليمن", "Yemen", "YE", 12.7797, 45.0367, "Asia/Aden"),

        // North Africa
        CityLocation("tripoli", "طرابلس", "Tripoli", "ليبيا", "Libya", "LY", 32.8872, 13.1913, "Africa/Tripoli"),
        CityLocation("benghazi", "بنغازي", "Benghazi", "ليبيا", "Libya", "LY", 32.1167, 20.0667, "Africa/Tripoli"),
        CityLocation("tunis", "تونس", "Tunis", "تونس", "Tunisia", "TN", 36.8065, 10.1815, "Africa/Tunis"),
        CityLocation("sfax", "صفاقس", "Sfax", "تونس", "Tunisia", "TN", 34.7406, 10.7603, "Africa/Tunis"),
        CityLocation("algiers", "الجزائر", "Algiers", "الجزائر", "Algeria", "DZ", 36.7538, 3.0588, "Africa/Algiers"),
        CityLocation("oran", "وهران", "Oran", "الجزائر", "Algeria", "DZ", 35.6987, -0.6349, "Africa/Algiers"),
        CityLocation("rabat", "الرباط", "Rabat", "المغرب", "Morocco", "MA", 34.0209, -6.8416, "Africa/Casablanca"),
        CityLocation("casablanca", "الدار البيضاء", "Casablanca", "المغرب", "Morocco", "MA", 33.5731, -7.5898, "Africa/Casablanca"),
        CityLocation("marrakech", "مراكش", "Marrakech", "المغرب", "Morocco", "MA", 31.6295, -7.9811, "Africa/Casablanca"),
        CityLocation("khartoum", "الخرطوم", "Khartoum", "السودان", "Sudan", "SD", 15.5007, 32.5599, "Africa/Khartoum"),

        // Turkey & Central Asia
        CityLocation("istanbul", "إسطنبول", "Istanbul", "تركيا", "Turkey", "TR", 41.0082, 28.9784, "Europe/Istanbul"),
        CityLocation("ankara", "أنقرة", "Ankara", "تركيا", "Turkey", "TR", 39.9334, 32.8597, "Europe/Istanbul"),
        CityLocation("baku", "باكو", "Baku", "أذربيجان", "Azerbaijan", "AZ", 40.4093, 49.8671, "Asia/Baku"),
        CityLocation("tashkent", "طشقند", "Tashkent", "أوزبكستان", "Uzbekistan", "UZ", 41.2995, 69.2401, "Asia/Tashkent"),
        CityLocation("samarkand", "سمرقند", "Samarkand", "أوزبكستان", "Uzbekistan", "UZ", 39.6270, 66.9750, "Asia/Tashkent"),

        // South & South-East Asia
        CityLocation("karachi", "كراتشي", "Karachi", "باكستان", "Pakistan", "PK", 24.8607, 67.0011, "Asia/Karachi"),
        CityLocation("lahore", "لاهور", "Lahore", "باكستان", "Pakistan", "PK", 31.5204, 74.3587, "Asia/Karachi"),
        CityLocation("islamabad", "إسلام آباد", "Islamabad", "باكستان", "Pakistan", "PK", 33.6844, 73.0479, "Asia/Karachi"),
        CityLocation("dhaka", "دكا", "Dhaka", "بنغلاديش", "Bangladesh", "BD", 23.8103, 90.4125, "Asia/Dhaka"),
        CityLocation("jakarta", "جاكرتا", "Jakarta", "إندونيسيا", "Indonesia", "ID", -6.2088, 106.8456, "Asia/Jakarta"),
        CityLocation("surabaya", "سورابايا", "Surabaya", "إندونيسيا", "Indonesia", "ID", -7.2575, 112.7521, "Asia/Jakarta"),
        CityLocation("kuala_lumpur", "كوالالمبور", "Kuala Lumpur", "ماليزيا", "Malaysia", "MY", 3.1390, 101.6869, "Asia/Kuala_Lumpur"),
        CityLocation("singapore", "سنغافورة", "Singapore", "سنغافورة", "Singapore", "SG", 1.3521, 103.8198, "Asia/Singapore"),

        // Europe & Americas
        CityLocation("london", "لندن", "London", "بريطانيا", "UK", "GB", 51.5074, -0.1278, "Europe/London"),
        CityLocation("paris", "باريس", "Paris", "فرنسا", "France", "FR", 48.8566, 2.3522, "Europe/Paris"),
        CityLocation("berlin", "برلين", "Berlin", "ألمانيا", "Germany", "DE", 52.5200, 13.4050, "Europe/Berlin"),
        CityLocation("new_york", "نيويورك", "New York", "الولايات المتحدة", "USA", "US", 40.7128, -74.0060, "America/New_York"),
        CityLocation("toronto", "تورونتو", "Toronto", "كندا", "Canada", "CA", 43.6532, -79.3832, "America/Toronto"),
        CityLocation("sydney", "سيدني", "Sydney", "أستراليا", "Australia", "AU", -33.8688, 151.2093, "Australia/Sydney")
    )

    fun findDefaultCity(): CityLocation {
        return list.first { it.id == "cairo" }
    }

    fun findCityById(id: String): CityLocation? {
        return list.find { it.id.equals(id, ignoreCase = true) }
    }

    fun searchCities(query: String): List<CityLocation> {
        val q = query.trim()
        if (q.isBlank()) return list
        return list.filter {
            it.nameAr.contains(q, ignoreCase = true) ||
                    it.nameEn.contains(q, ignoreCase = true) ||
                    it.countryAr.contains(q, ignoreCase = true) ||
                    it.countryEn.contains(q, ignoreCase = true)
        }
    }
}
