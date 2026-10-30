package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.magicnav.MagicNavItem
import com.example.magicnav.MagicNavigationMenu
import com.example.ui.components.MiniPlayer
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.CountriesScreen
import com.example.ui.screens.CategoryStationsScreen
import com.example.ui.screens.CountryDetailScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.FullPlayerScreen
import com.example.ui.screens.GroupKhatmaDetailScreen
import com.example.ui.screens.GroupKhatmaListScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PrayerTimesScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.ReciterDetailScreen
import com.example.ui.screens.RecitersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.SkyBluePrimary
import com.example.viewmodel.GroupKhatmaViewModel
import com.example.viewmodel.PersonalKhatmaViewModel
import com.example.viewmodel.PrayerViewModel
import com.example.viewmodel.RadioViewModel
import kotlinx.coroutines.flow.first

@Composable
fun QuranRadioApp(
    viewModel: RadioViewModel,
    prayerViewModel: PrayerViewModel = viewModel(),
    groupKhatmaViewModel: GroupKhatmaViewModel = viewModel(),
    personalKhatmaViewModel: PersonalKhatmaViewModel = viewModel(),
    initialStationId: String? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val playerState by viewModel.playerState.collectAsState()
    val currentLanguage by viewModel.appLanguage.collectAsState()

    LaunchedEffect(initialStationId) {
        val stationId = initialStationId?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        val station = viewModel.allStations.value.firstOrNull { it.id == stationId }
            ?: viewModel.allStations.first { stations -> stations.isNotEmpty() }
                .firstOrNull { it.id == stationId }
        if (station != null) {
            viewModel.playStation(station)
            navController.navigate(Screen.FullPlayer.route) {
                launchSingleTop = true
            }
        } else {
            android.util.Log.w("QuranRadioApp", "Notification station not found: $stationId")
        }
    }

    // Show bottom bar and miniplayer on main bottom tab screens
    val isBottomBarVisible = when (currentRoute) {
        Screen.Home.route,
        Screen.Prayer.route,
        Screen.Qibla.route,
        Screen.Countries.route,
        Screen.Reciters.route,
        Screen.GroupKhatma.route,
        Screen.Settings.route -> true
        else -> false
    }

    val isFullPlayerOpen = currentRoute == Screen.FullPlayer.route

    val magicNavItems = remember {
        listOf(
            MagicNavItem(
                route = Screen.Home.route,
                stringKey = BottomNavItem.Home.stringKey,
                selectedIcon = BottomNavItem.Home.selectedIcon,
                unselectedIcon = BottomNavItem.Home.unselectedIcon,
                accentColor = Color(0xFF059669) // أخضر إسلامي حيوي للرئيسية
            ),
            MagicNavItem(
                route = Screen.Countries.route,
                stringKey = BottomNavItem.Countries.stringKey,
                selectedIcon = BottomNavItem.Countries.selectedIcon,
                unselectedIcon = BottomNavItem.Countries.unselectedIcon,
                accentColor = Color(0xFF0284C7) // أزرق سماوي للدول
            ),
            MagicNavItem(
                route = Screen.Reciters.route,
                stringKey = BottomNavItem.Reciters.stringKey,
                selectedIcon = BottomNavItem.Reciters.selectedIcon,
                unselectedIcon = BottomNavItem.Reciters.unselectedIcon,
                accentColor = Color(0xFFD97706) // ذهبي عنبري للقراء
            ),
            MagicNavItem(
                route = Screen.GroupKhatma.route,
                stringKey = BottomNavItem.GroupKhatma.stringKey,
                selectedIcon = BottomNavItem.GroupKhatma.selectedIcon,
                unselectedIcon = BottomNavItem.GroupKhatma.unselectedIcon,
                accentColor = Color(0xFF8B5CF6) // بنفسجي للختمات
            ),
            MagicNavItem(
                route = Screen.Prayer.route,
                stringKey = BottomNavItem.Prayer.stringKey,
                selectedIcon = BottomNavItem.Prayer.selectedIcon,
                unselectedIcon = BottomNavItem.Prayer.unselectedIcon,
                accentColor = Color(0xFF0EA5E9) // فيروزي للمواقيت
            ),
            MagicNavItem(
                route = Screen.Qibla.route,
                stringKey = BottomNavItem.Qibla.stringKey,
                selectedIcon = BottomNavItem.Qibla.selectedIcon,
                unselectedIcon = BottomNavItem.Qibla.unselectedIcon,
                accentColor = Color(0xFFF59E0B) // كهرماني للقبلة
            ),
            MagicNavItem(
                route = Screen.Settings.route,
                stringKey = BottomNavItem.Settings.stringKey,
                selectedIcon = BottomNavItem.Settings.selectedIcon,
                unselectedIcon = BottomNavItem.Settings.unselectedIcon,
                accentColor = Color(0xFF64748B) // سليت للإعدادات
            )
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (isBottomBarVisible) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // MiniPlayer positioned clearly above Bottom Navigation Bar
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
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Magic Navigation Menu with Animated Floating Bubble
                    MagicNavigationMenu(
                        items = magicNavItems,
                        currentRoute = currentRoute,
                        onItemSelected = { item ->
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp)
                            .testTag("main_bottom_nav_bar")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (initialStationId.isNullOrBlank()) Screen.Splash.route else Screen.FullPlayer.route,
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
                    prayerViewModel = prayerViewModel,
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
                    onNavigateToPrayer = {
                        navController.navigate(Screen.Prayer.route) { launchSingleTop = true }
                    },
                    onNavigateToQibla = {
                        navController.navigate(Screen.Qibla.route) { launchSingleTop = true }
                    },
                    onNavigateToGroupKhatma = {
                        navController.navigate(Screen.GroupKhatma.route) { launchSingleTop = true }
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route) { launchSingleTop = true }
                    },
                    onOpenCategory = { category ->
                        navController.navigate(Screen.CategoryStations.createRoute(category))
                    },
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )
            }

            // Dedicated category screen; category content no longer expands below Home.
            composable(
                route = Screen.CategoryStations.route,
                arguments = listOf(navArgument("category") { type = NavType.StringType })
            ) { backStackEntry ->
                val categoryName = backStackEntry.arguments?.getString("category")
                val category = com.example.model.StationCategory.values().firstOrNull { it.name == categoryName }
                    ?: com.example.model.StationCategory.OFFICIAL
                CategoryStationsScreen(
                    category = category,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToStationDetail = { station ->
                        viewModel.playStation(station)
                        navController.navigate(Screen.FullPlayer.route)
                    },
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )
            }

            // Prayer Times Screen
            composable(Screen.Prayer.route) {
                PrayerTimesScreen(
                    viewModel = prayerViewModel,
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )
            }

            // Qibla Direction Screen
            composable(Screen.Qibla.route) {
                QiblaScreen(
                    viewModel = prayerViewModel,
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )
            }

            // Countries Screen
            composable(Screen.Countries.route) {
                CountriesScreen(
                    viewModel = viewModel,
                    onCountryClick = { country ->
                        navController.navigate(Screen.CountryDetail.createRoute(country.code))
                    },
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
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
                    onStationClick = { station ->
                        viewModel.playStation(station)
                        navController.navigate(Screen.FullPlayer.route)
                    },
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
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

            // Group Khatma List Screen
            composable(Screen.GroupKhatma.route) {
                GroupKhatmaListScreen(
                    viewModel = groupKhatmaViewModel,
                    personalKhatmaViewModel = personalKhatmaViewModel,
                    radioViewModel = viewModel,
                    onNavigateToDetail = { khatmaId ->
                        navController.navigate(Screen.KhatmaDetail.createRoute(khatmaId))
                    },
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )
            }

            // Group Khatma Detail Screen
            composable(
                route = Screen.KhatmaDetail.route,
                arguments = listOf(navArgument("khatmaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val khatmaId = backStackEntry.arguments?.getString("khatmaId") ?: ""
                GroupKhatmaDetailScreen(
                    khatmaId = khatmaId,
                    viewModel = groupKhatmaViewModel,
                    onBackClick = { navController.popBackStack() },
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )
            }

            // Favorites Screen
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )
            }

            // Settings Screen
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    prayerViewModel = prayerViewModel,
                    groupKhatmaViewModel = groupKhatmaViewModel,
                    onNavigateToAbout = {
                        navController.navigate(Screen.About.route)
                    },
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
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
