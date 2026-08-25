package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SkyBluePrimary,
    onPrimary = NavyPrimary,
    primaryContainer = SkyBlueDark,
    onPrimaryContainer = PureWhite,
    secondary = IslamicMint,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = PureWhite,
    tertiary = LiveCoral,
    onTertiary = PureWhite,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = SkyBluePrimary,
    onPrimary = PureWhite,
    primaryContainer = SkyBlueContainer,
    onPrimaryContainer = NavyPrimary,
    secondary = IslamicGreenPrimary,
    onSecondary = PureWhite,
    secondaryContainer = SkyBlueLight,
    onSecondaryContainer = NavyPrimary,
    tertiary = LiveCoral,
    onTertiary = PureWhite,
    tertiaryContainer = SkyBlueLight,
    onTertiaryContainer = LiveCoral,
    background = SoftSkyBackground,
    onBackground = NavyPrimary,
    surface = CardSurface,
    onSurface = NavyPrimary,
    surfaceVariant = CardSurfaceVariant,
    onSurfaceVariant = NavySecondary,
    outline = BorderSubtle
)

@Composable
fun QuranRadioTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false, // Preserve our custom curated Professional Polish theme colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
