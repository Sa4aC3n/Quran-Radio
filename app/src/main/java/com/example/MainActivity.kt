package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import com.example.model.AppThemeMode
import com.example.player.QuranMediaService
import com.example.ui.navigation.QuranRadioApp
import com.example.ui.theme.QuranRadioTheme
import com.example.viewmodel.RadioViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RadioViewModel by viewModels()
    private val pendingStationId = mutableStateOf<String?>(null)

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission granted or denied handled gracefully
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request POST_NOTIFICATIONS on Android 13+
        checkNotificationPermission()

        // Handle possible deep link station ID
        val deepLinkStationId = extractDeepLinkStationId(intent)
        pendingStationId.value = deepLinkStationId

        // Handle possible deep link khatma
        handleKhatmaIntent(intent)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val currentLanguage by viewModel.appLanguage.collectAsState()

            val isDark = when (themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            val layoutDirection = if (currentLanguage.isRtl) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }

            QuranRadioTheme(darkTheme = isDark) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides layoutDirection,
                    com.example.localization.LocalAppLanguage provides currentLanguage
                ) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        QuranRadioApp(
                            viewModel = viewModel,
                            initialStationId = pendingStationId.value
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingStationId.value = extractDeepLinkStationId(intent)
        handleKhatmaIntent(intent)
    }

    private fun handleKhatmaIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "quranradio" && uri.host == "khatma") {
            lifecycleScope.launch {
                try {
                    val groupKhatmaRepository = com.example.data.GroupKhatmaRepository(this@MainActivity)
                    val userProfile = groupKhatmaRepository.userProfile.first()
                    groupKhatmaRepository.joinKhatmaByCode(uri.toString(), userProfile.userName)
                } catch (e: Exception) {
                    android.util.Log.w("MainActivity", "Failed to resolve deep link khatma: ${e.message}")
                }
            }
        }
    }

    private fun handlePlayStationId(stationId: String) {
        lifecycleScope.launch {
            try {
                val stations = viewModel.allStations.first { it.isNotEmpty() }
                val station = stations.find { it.id == stationId }
                if (station != null) {
                    viewModel.playStation(station)
                }
            } catch (e: Exception) {
                android.util.Log.w("MainActivity", "Failed to resolve deep link station: ${e.message}")
            }
        }
    }

    private fun extractDeepLinkStationId(intent: Intent?): String? {
        val extraId = intent?.getStringExtra("EXTRA_AUTO_PLAY_STATION_ID")
        if (!extraId.isNullOrBlank()) {
            return extraId
        }
        val data = intent?.data ?: return null
        if (data.scheme == "quranradio" && data.host == "station") {
            return data.lastPathSegment
        }
        return null
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Resume immediate in-app update if in progress
        try {
            val appUpdateManager = com.google.android.play.core.appupdate.AppUpdateManagerFactory.create(this)
            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == com.google.android.play.core.install.model.UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            this,
                            com.google.android.play.core.appupdate.AppUpdateOptions.defaultOptions(
                                com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
                            ),
                            com.example.util.REQUEST_CODE_APP_UPDATE
                        )
                    } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {}
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == com.example.util.REQUEST_CODE_APP_UPDATE) {
            if (resultCode != RESULT_OK) {
                android.util.Log.d("MainActivity", "App update flow completed or canceled with resultCode: $resultCode")
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        if (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP) {
            val app = application as? QuranRadioApplication
            if (app?.adhanManager?.isAdhanActive?.value == true) {
                if (app.adhanManager.handleVolumeKeyEvent()) {
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
