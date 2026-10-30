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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.PersonalKhatmaViewModel

/**
 * Modern Khatmat Section (Personal Khatma & Group Khatma).
 * Matches the bottom section before navigation bar in the mockup.
 */
@Composable
fun ModernKhatmatSection(
    personalKhatmaViewModel: PersonalKhatmaViewModel?,
    onNavigateToPersonalKhatma: () -> Unit,
    onNavigateToGroupKhatma: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val activeKhatma = personalKhatmaViewModel?.activeKhatma?.collectAsState()?.value

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header: "الختمات" and "عرض الكل >"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(ModernColors.TileIconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = ModernColors.TileIconTint,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = "الختمات",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) ModernColors.DarkTextPrimary else ModernColors.TextPrimary
                )
            }

            TextButton(
                onClick = onNavigateToGroupKhatma,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "عرض الكل",
                        fontSize = 12.sp,
                        color = ModernColors.TileIconTint,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos, // RTL point left
                        contentDescription = null,
                        tint = ModernColors.TileIconTint,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }

        // Two Cards: Personal Khatma & Group Khatma
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Right Card (RTL): ختمة خاصة
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(95.dp)
                    .shadow(2.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onNavigateToPersonalKhatma),
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) ModernColors.DarkCardBackground else Color.White,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isDark) ModernColors.DarkCardBorder else ModernColors.CardBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "ختمة خاصة",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) ModernColors.DarkTextPrimary else ModernColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (activeKhatma != null && activeKhatma.completedCount > 0) {
                                "أنجزت ${activeKhatma.completedCount} من 30 جزء"
                            } else {
                                "اختر القارئ والمدة التي تناسبك"
                            },
                            fontSize = 9.5.sp,
                            color = if (isDark) ModernColors.DarkTextSecondary else ModernColors.TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ModernColors.TileIconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = ModernColors.TileIconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 2. Left Card (RTL): ختمة جماعية
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(95.dp)
                    .shadow(2.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onNavigateToGroupKhatma),
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) ModernColors.DarkCardBackground else Color.White,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isDark) ModernColors.DarkCardBorder else ModernColors.CardBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "ختمة جماعية",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) ModernColors.DarkTextPrimary else ModernColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "مع مجموعة من القراء والمستمعين",
                            fontSize = 9.5.sp,
                            color = if (isDark) ModernColors.DarkTextSecondary else ModernColors.TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
