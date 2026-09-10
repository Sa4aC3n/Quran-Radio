package com.example.localization

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StationCategory
import com.example.ui.theme.SkyBluePrimary

/**
 * CompositionLocal providing current AppLanguage down the Compose hierarchy.
 */
val LocalAppLanguage = compositionLocalOf { AppLanguage.AR }

/**
 * Composable helper to retrieve localized string for current language.
 */
@Composable
@ReadOnlyComposable
fun appString(key: StringKey): String {
    val language = LocalAppLanguage.current
    return AppStrings.get(key, language)
}

/**
 * Composable helper with formatting arguments.
 */
@Composable
@ReadOnlyComposable
fun appString(key: StringKey, vararg args: Any): String {
    val language = LocalAppLanguage.current
    return AppStrings.format(key, language, *args)
}

/**
 * Extension on StationCategory to get localized title based on current language.
 */
fun StationCategory.localizedTitle(language: AppLanguage): String {
    val key = when (this) {
        StationCategory.OFFICIAL -> StringKey.CAT_OFFICIAL
        StationCategory.QURAN -> StringKey.CAT_QURAN
        StationCategory.RECITER -> StringKey.CAT_RECITER
        StationCategory.TAFSIR, StationCategory.TAFSEER -> StringKey.CAT_TAFSIR
        StationCategory.TRANSLATION -> StringKey.CAT_TRANSLATION
        StationCategory.SEERAH_AND_STORIES, StationCategory.SEERAH -> StringKey.CAT_SEERAH_AND_STORIES
        StationCategory.RECITATION -> StringKey.CAT_RECITATION
        StationCategory.RUQYAH, StationCategory.ROQIAH -> StringKey.CAT_RUQYAH
        StationCategory.FATWA, StationCategory.FATAWA -> StringKey.CAT_FATWA
        StationCategory.ADHKAR -> StringKey.CAT_ADHKAR
        StationCategory.SUNNAH -> StringKey.CAT_SUNNAH
        StationCategory.ISLAMIC -> StringKey.CAT_OFFICIAL
    }
    return AppStrings.get(key, language)
}

/**
 * Extension on RadioStation to get localized name based on current language.
 */
fun com.example.model.RadioStation.localizedName(language: AppLanguage): String {
    return StationTranslator.getStationName(this, language)
}

/**
 * Extension on Country to get localized name based on current language.
 */
fun com.example.model.Country.localizedName(language: AppLanguage): String {
    return StationTranslator.getCountryName(this, language)
}

/**
 * Extension on Reciter to get localized name based on current language.
 */
fun com.example.model.Reciter.localizedName(language: AppLanguage): String {
    return StationTranslator.getReciterName(this, language)
}


/**
 * A dialog allowing the user to select one of the 7 supported languages.
 */
@Composable
fun LanguageSelectionDialog(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    val lang = LocalAppLanguage.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SkyBluePrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = SkyBluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = AppStrings.get(StringKey.LANGUAGE_DIALOG_TITLE, lang),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = AppStrings.get(StringKey.LANGUAGE_DIALOG_SUBTITLE, lang),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AppLanguage.entries) { languageItem ->
                    val isSelected = languageItem == currentLanguage
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                onLanguageSelected(languageItem)
                                onDismiss()
                            }
                            .testTag("lang_item_${languageItem.code}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                SkyBluePrimary.copy(alpha = 0.12f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            }
                        ),
                        border = if (isSelected) {
                            BorderStroke(1.5.dp, SkyBluePrimary)
                        } else {
                            BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = languageItem.flag,
                                    fontSize = 24.sp
                                )
                                Column {
                                    Text(
                                        text = languageItem.nativeName,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) SkyBluePrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = "${languageItem.englishName} • ${if (languageItem.isRtl) "RTL" else "LTR"}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(SkyBluePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("lang_dialog_close")
            ) {
                Text(
                    text = AppStrings.get(StringKey.ACTION_CLOSE, lang),
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
