package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Prayer : Screen("prayer")
    object Qibla : Screen("qibla")
    object Countries : Screen("countries")
    object CountryDetail : Screen("country/{countryCode}") {
        fun createRoute(countryCode: String) = "country/$countryCode"
    }
    object Reciters : Screen("reciters")
    object ReciterDetail : Screen("reciter/{reciterId}") {
        fun createRoute(reciterId: String) = "reciter/$reciterId"
    }
    object GroupKhatma : Screen("group_khatma")
    object KhatmaDetail : Screen("khatma/{khatmaId}") {
        fun createRoute(khatmaId: String) = "khatma/$khatmaId"
    }
    object Favorites : Screen("favorites")
    object Settings : Screen("settings")
    object About : Screen("about")
    object FullPlayer : Screen("player")
}

sealed class BottomNavItem(
    val route: String,
    val stringKey: com.example.localization.StringKey,
    val titleAr: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem(
        route = Screen.Home.route,
        stringKey = com.example.localization.StringKey.NAV_HOME,
        titleAr = "الرئيسية",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    object Prayer : BottomNavItem(
        route = Screen.Prayer.route,
        stringKey = com.example.localization.StringKey.NAV_PRAYER,
        titleAr = "المواقيت",
        selectedIcon = Icons.Filled.AccessTime,
        unselectedIcon = Icons.Outlined.AccessTime
    )

    object Qibla : BottomNavItem(
        route = Screen.Qibla.route,
        stringKey = com.example.localization.StringKey.NAV_QIBLA,
        titleAr = "القبلة",
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore
    )

    object Countries : BottomNavItem(
        route = Screen.Countries.route,
        stringKey = com.example.localization.StringKey.NAV_COUNTRIES,
        titleAr = "الدول",
        selectedIcon = Icons.Filled.Public,
        unselectedIcon = Icons.Outlined.Public
    )

    object Reciters : BottomNavItem(
        route = Screen.Reciters.route,
        stringKey = com.example.localization.StringKey.NAV_RECITERS,
        titleAr = "القراء",
        selectedIcon = Icons.Filled.Mic,
        unselectedIcon = Icons.Outlined.Mic
    )

    object GroupKhatma : BottomNavItem(
        route = Screen.GroupKhatma.route,
        stringKey = com.example.localization.StringKey.NAV_GROUP_KHATMA,
        titleAr = "الختمات",
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook
    )

    object Favorites : BottomNavItem(
        route = Screen.Favorites.route,
        stringKey = com.example.localization.StringKey.NAV_FAVORITES,
        titleAr = "المفضلة",
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder
    )

    object Settings : BottomNavItem(
        route = Screen.Settings.route,
        stringKey = com.example.localization.StringKey.NAV_SETTINGS,
        titleAr = "الإعدادات",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    companion object {
        val items = listOf(Home, Countries, Reciters, GroupKhatma, Prayer, Qibla, Settings)
    }
}
