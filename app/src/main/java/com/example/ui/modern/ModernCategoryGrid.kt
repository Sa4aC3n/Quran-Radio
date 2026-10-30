package com.example.ui.modern

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StationCategory

/**
 * Model representing a modern category tile.
 */
data class ModernCategoryItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val category: StationCategory? = null,
    val onClick: () -> Unit
)

/**
 * 12 Modern Category Tiles Grid (3 Rows x 4 Columns).
 * Directly matches the 12 feature boxes in the design mockup.
 */
@Composable
fun ModernCategoryGrid(
    onSelectCategory: (StationCategory) -> Unit,
    onNavigateToCountries: () -> Unit,
    onNavigateToReciters: () -> Unit,
    onNavigateToQibla: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 12 Items mapped 1:1 to the image
    val items = listOf(
        // Row 1
        ModernCategoryItem(
            title = "تلاوات خاشعة",
            subtitle = "تأخذك إلى عالم آخر",
            icon = Icons.Default.AutoStories,
            category = StationCategory.RECITATION,
            onClick = { onSelectCategory(StationCategory.RECITATION) }
        ),
        ModernCategoryItem(
            title = "إذاعات رسمية",
            subtitle = "هيئات الإذاعة",
            icon = Icons.Default.Radio,
            category = StationCategory.OFFICIAL,
            onClick = { onSelectCategory(StationCategory.OFFICIAL) }
        ),
        ModernCategoryItem(
            title = "إذاعات القراء",
            subtitle = "أشهر القراء",
            icon = Icons.Default.Mic,
            category = StationCategory.RECITER,
            onClick = onNavigateToReciters
        ),
        ModernCategoryItem(
            title = "إذاعات الدول",
            subtitle = "من مختلف دول العالم",
            icon = Icons.Default.Public,
            onClick = onNavigateToCountries
        ),

        // Row 2
        ModernCategoryItem(
            title = "السيرة والقصص",
            subtitle = "عبر ودروس",
            icon = Icons.Default.Mosque,
            category = StationCategory.SEERAH_AND_STORIES,
            onClick = { onSelectCategory(StationCategory.SEERAH_AND_STORIES) }
        ),
        ModernCategoryItem(
            title = "السنة النبوية",
            subtitle = "شرح وبيان",
            icon = Icons.Default.AccountBalance,
            category = StationCategory.SUNNAH,
            onClick = { onSelectCategory(StationCategory.SUNNAH) }
        ),
        ModernCategoryItem(
            title = "ترجمات المعاني",
            subtitle = "بأكثر من لغة",
            icon = Icons.Default.Translate,
            category = StationCategory.TRANSLATION,
            onClick = { onSelectCategory(StationCategory.TRANSLATION) }
        ),
        ModernCategoryItem(
            title = "تفسير القرآن",
            subtitle = "بأصوات مميزة",
            icon = Icons.Default.MenuBook,
            category = StationCategory.TAFSIR,
            onClick = { onSelectCategory(StationCategory.TAFSIR) }
        ),

        // Row 3
        ModernCategoryItem(
            title = "القبلة",
            subtitle = "اتجاهك في أي مكان",
            icon = Icons.Default.Explore,
            onClick = onNavigateToQibla
        ),
        ModernCategoryItem(
            title = "الرقية الشرعية",
            subtitle = "للاطمئنان والشفاء",
            icon = Icons.Default.Spa,
            category = StationCategory.RUQYAH,
            onClick = { onSelectCategory(StationCategory.RUQYAH) }
        ),
        ModernCategoryItem(
            title = "أذكار الصباح والمساء",
            subtitle = "راحة وطمأنينة",
            icon = Icons.Default.WbSunny,
            category = StationCategory.ADHKAR,
            onClick = { onSelectCategory(StationCategory.ADHKAR) }
        ),
        ModernCategoryItem(
            title = "الفتاوى والأحكام",
            subtitle = "من علماء موثوقين",
            icon = Icons.Default.Balance,
            category = StationCategory.FATWA,
            onClick = { onSelectCategory(StationCategory.FATWA) }
        )
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Render 3 rows with 4 items each
        for (rowIndex in 0 until 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (colIndex in 0 until 4) {
                    val itemIndex = rowIndex * 4 + colIndex
                    if (itemIndex < items.size) {
                        val item = items[itemIndex]
                        ModernCategoryTile(
                            item = item,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Category Tile
 */
@Composable
private fun ModernCategoryTile(
    item: ModernCategoryItem,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Surface(
        modifier = modifier
            .height(108.dp)
            .shadow(1.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = item.onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) ModernColors.DarkCardBackground else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isDark) ModernColors.DarkCardBorder else ModernColors.CardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon in circular light-blue badge
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isDark) ModernColors.DarkTileIconBg else ModernColors.TileIconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = if (isDark) ModernColors.DarkTileIconTint else ModernColors.TileIconTint,
                    modifier = Modifier.size(19.dp)
                )
            }

            // Title with small arrow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.title,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) ModernColors.DarkTextPrimary else ModernColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(2.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(8.dp)
                )
            }

            // Subtitle
            Text(
                text = item.subtitle,
                fontSize = 9.sp,
                fontWeight = FontWeight.Normal,
                color = if (isDark) ModernColors.DarkTextSecondary else ModernColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
