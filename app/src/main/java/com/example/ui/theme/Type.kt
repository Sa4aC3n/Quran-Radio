package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Cairo variable font is bundled in res/font, so it is available without network access.
 * Android 7.x does not support variable-font axes; use the system sans-serif fallback there.
 */
@OptIn(ExperimentalTextApi::class)
val CairoFontFamily: FontFamily = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    FontFamily(
        Font(R.font.cairo_variable, FontWeight.W200, FontStyle.Normal, FontVariation.Settings(FontWeight.W200, FontStyle.Normal)),
        Font(R.font.cairo_variable, FontWeight.W300, FontStyle.Normal, FontVariation.Settings(FontWeight.W300, FontStyle.Normal)),
        Font(R.font.cairo_variable, FontWeight.W400, FontStyle.Normal, FontVariation.Settings(FontWeight.W400, FontStyle.Normal)),
        Font(R.font.cairo_variable, FontWeight.W500, FontStyle.Normal, FontVariation.Settings(FontWeight.W500, FontStyle.Normal)),
        Font(R.font.cairo_variable, FontWeight.W600, FontStyle.Normal, FontVariation.Settings(FontWeight.W600, FontStyle.Normal)),
        Font(R.font.cairo_variable, FontWeight.W700, FontStyle.Normal, FontVariation.Settings(FontWeight.W700, FontStyle.Normal)),
        Font(R.font.cairo_variable, FontWeight.W800, FontStyle.Normal, FontVariation.Settings(FontWeight.W800, FontStyle.Normal)),
        Font(R.font.cairo_variable, FontWeight.W900, FontStyle.Normal, FontVariation.Settings(FontWeight.W900, FontStyle.Normal)),
        Font(R.font.cairo_variable, FontWeight(1000), FontStyle.Normal, FontVariation.Settings(FontWeight(1000), FontStyle.Normal))
    )
} else {
    FontFamily.SansSerif
}

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp
    ),
    displayMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)
