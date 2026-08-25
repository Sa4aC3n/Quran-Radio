package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.ui.components.MiniPlayer
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.CountriesScreen
import com.example.ui.screens.CountryDetailScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.FullPlayerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReciterDetailScreen
import com.example.ui.screens.RecitersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.SkyBluePrimary
import com.example.viewmodel.RadioViewModel

@Composable
fun QuranRadioApp(
    viewModel: RadioViewModel,
    initialStationId: String? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val playerState by viewModel.playerState.collectAsState()

    // Show bottom bar and miniplayer only on main bottom tab screens
    val isBottomBarVisible = when (currentRoute) {
        Screen.Home.route,
        Screen.Countries.route,
        Screen.Reciters.route,
        Screen.Favorites.route,
        Screen.Settings.route -> true
        else -> false
    }

    val isFullPlayerOpen = currentRoute == Screen.FullPlayer.route

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                Box {
                    // MiniPlayer above Bottom Navigation Bar
                    MiniPlayer(
                        playerState = playerState,
                        onExpandFullPlayer = {
                            navController.navigate(Screen.FullPlayer.route)
                        },
                        onTogglePlayPause = {
                            viewModel.togglePlayPause()
                        },
                        onClosePlayer = {
                            viewModel.stop()
                        },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(bottom = 76.dp)
                    )

                    // Bottom Navigation Bar
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .testTag("main_bottom_nav_bar")
                    ) {
                        BottomNavItem.items.forEach { item ->
                            val isSelected = currentRoute == item.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.titleAr
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.titleAr,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SkyBluePrimary,
                                    selectedTextColor = SkyBluePrimary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_item_${item.route}")
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.fillMaxSize()
        ) {
            // Splash Screen
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Home Screen
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToStationDetail = {
                        viewModel.playStation(it)
                        navController.navigate(Screen.FullPlayer.route)
                    },
                    onNavigateToCountries = {
                        navController.navigate(Screen.Countries.route)
                    },
                    onNavigateToReciters = {
                        navController.navigate(Screen.Reciters.route)
                    },
                    onNavigateToFavorites = {
                        navController.navigate(Screen.Favorites.route)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            // Countries Screen
            composable(Screen.Countries.route) {
                CountriesScreen(
                    viewModel = viewModel,
                    onCountryClick = { country ->
                        navController.navigate(Screen.CountryDetail.createRoute(country.code))
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            // Country Detail Screen
            composable(
                route = Screen.CountryDetail.route,
                arguments = listOf(navArgument("countryCode") { type = NavType.StringType })
            ) { backStackEntry ->
                val countryCode = backStackEntry.arguments?.getString("countryCode") ?: "EG"
                CountryDetailScreen(
                    countryCode = countryCode,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToStationDetail = {
                        viewModel.playStation(it)
                        navController.navigate(Screen.FullPlayer.route)
                    }
                )
            }

            // Reciters Screen
            composable(Screen.Reciters.route) {
                RecitersScreen(
                    viewModel = viewModel,
                    onReciterClick = { reciter ->
                        navController.navigate(Screen.ReciterDetail.createRoute(reciter.id))
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            // Reciter Detail Screen
            composable(
                route = Screen.ReciterDetail.route,
                arguments = listOf(navArgument("reciterId") { type = NavType.StringType })
            ) { backStackEntry ->
                val reciterId = backStackEntry.arguments?.getString("reciterId") ?: ""
                ReciterDetailScreen(
                    reciterId = reciterId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Favorites Screen
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            // Settings Screen
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToAbout = {
                        navController.navigate(Screen.About.route)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            // About Screen
            composable(Screen.About.route) {
                AboutScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Full Player Screen (Modal / Full View with Deep link support)
            composable(
                route = Screen.FullPlayer.route,
                deepLinks = listOf(
                    navDeepLink { uriPattern = "quranradio://station/{stationId}" }
                )
            ) {
                FullPlayerScreen(
                    viewModel = viewModel,
                    onCollapse = { navController.popBackStack() }
                )
            }
        }
    }
}
