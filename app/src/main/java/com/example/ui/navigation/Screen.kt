package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Countries : Screen("countries")
    object CountryDetail : Screen("country/{countryCode}") {
        fun createRoute(countryCode: String) = "country/$countryCode"
    }
    object Reciters : Screen("reciters")
    object ReciterDetail : Screen("reciter/{reciterId}") {
        fun createRoute(reciterId: String) = "reciter/$reciterId"
    }
    object Favorites : Screen("favorites")
    object Settings : Screen("settings")
    object About : Screen("about")
    object FullPlayer : Screen("player")
}

sealed class BottomNavItem(
    val route: String,
    val titleAr: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem(
        route = Screen.Home.route,
        titleAr = "الرئيسية",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    object Countries : BottomNavItem(
        route = Screen.Countries.route,
        titleAr = "الدول",
        selectedIcon = Icons.Filled.Public,
        unselectedIcon = Icons.Outlined.Public
    )

    object Reciters : BottomNavItem(
        route = Screen.Reciters.route,
        titleAr = "القراء",
        selectedIcon = Icons.Filled.Mic,
        unselectedIcon = Icons.Outlined.Mic
    )

    object Favorites : BottomNavItem(
        route = Screen.Favorites.route,
        titleAr = "المفضلة",
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder
    )

    object Settings : BottomNavItem(
        route = Screen.Settings.route,
        titleAr = "الإعدادات",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    companion object {
        val items = listOf(Home, Countries, Reciters, Favorites, Settings)
    }
}
