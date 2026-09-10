package com.example.magicnav

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.StringKey
import com.example.localization.appString

/**
 * عنصر بيانات لقائمة التنقل السحرية MagicNavigationMenu
 */
data class MagicNavItem(
    val route: String,
    val stringKey: StringKey,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val accentColor: Color
)

/**
 * القائمة الافتراضية لأقسام التطبيق
 */
val defaultAppNavItems = listOf(
    MagicNavItem(
        route = "home",
        stringKey = StringKey.NAV_HOME,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        accentColor = Color(0xFF059669) // أخضر إسلامي حيوي
    ),
    MagicNavItem(
        route = "countries",
        stringKey = StringKey.NAV_COUNTRIES,
        selectedIcon = Icons.Filled.Public,
        unselectedIcon = Icons.Outlined.Public,
        accentColor = Color(0xFF0284C7) // أزرق سماوي
    ),
    MagicNavItem(
        route = "reciters",
        stringKey = StringKey.NAV_RECITERS,
        selectedIcon = Icons.Filled.Mic,
        unselectedIcon = Icons.Outlined.Mic,
        accentColor = Color(0xFFD97706) // ذهبي عنبري
    ),
    MagicNavItem(
        route = "group_khatma",
        stringKey = StringKey.NAV_GROUP_KHATMA,
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook,
        accentColor = Color(0xFF8B5CF6) // بنفسجي روحاني
    ),
    MagicNavItem(
        route = "prayer",
        stringKey = StringKey.NAV_PRAYER,
        selectedIcon = Icons.Filled.AccessTime,
        unselectedIcon = Icons.Outlined.AccessTime,
        accentColor = Color(0xFF0EA5E9) // فيروزي
    ),
    MagicNavItem(
        route = "qibla",
        stringKey = StringKey.NAV_QIBLA,
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore,
        accentColor = Color(0xFFF59E0B) // كهرماني ذهبي
    ),
    MagicNavItem(
        route = "settings",
        stringKey = StringKey.NAV_SETTINGS,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        accentColor = Color(0xFF64748B) // سليت أنيق
    )
)

/**
 * شريط التنقل السحري MagicNavigationMenu عالي الأداء وفائق السرعة
 * تم إزالة الفراغات الزائدة بين الكلمة والأيقونة وتحسين الرسوم المتحركة
 * لتعمل بأعلى معدل إطارات (60-120fps) واستجابة فورية للنقر.
 */
@Composable
fun MagicNavigationMenu(
    modifier: Modifier = Modifier,
    items: List<MagicNavItem> = defaultAppNavItems,
    currentRoute: String? = null,
    onItemSelected: (MagicNavItem) -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag("magic_navigation_menu"),
        contentAlignment = Alignment.BottomCenter
    ) {
        // الحاوية الكلية بارتفاع مدمج ومتناسق
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // خلفية الشريط السفلي المقوسة الأنيقة
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
            ) {}

            // طبقة الأيقونات والتفاعلات
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                items.forEach { item ->
                    val isSelected = item.route == currentRoute
                    val interactionSource = remember { MutableInteractionSource() }

                    MagicNavTabItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onItemSelected(item) },
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

/**
 * خانة كل تبويب في القائمة السحرية
 * تم تقريب الأيقونة من الكلمة تحتها بدقة، مع تسريع الحركة والاستجابة الفورية.
 */
@Composable
private fun MagicNavTabItem(
    item: MagicNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier
) {
    val title = appString(item.stringKey)

    // حركة رفع سريعة وخفيفة على المعالج
    val offsetY by animateDpAsState(
        targetValue = if (isSelected) (-8).dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "navOffsetY"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) item.accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "navTextColor"
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick()
            }
            .padding(bottom = 6.dp)
            .testTag("nav_item_${item.route}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // حاوية الأيقونة مع إزاحة خفيفة متناسقة تمنع الفراغ المفرط
        Box(
            modifier = Modifier
                .size(38.dp)
                .offset(y = offsetY),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                // الفقاعة الدائرية الملونة للعنصر النشط
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .shadow(4.dp, CircleShape)
                        .background(item.accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.selectedIcon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                // الأيقونة غير النشطة في وضع الراحة
                Icon(
                    imageVector = item.unselectedIcon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // مسافة صغيرة ومحكمة بين الأيقونة والكلمة تحتها
        Spacer(modifier = Modifier.height(1.dp))

        Text(
            text = title,
            fontSize = if (isSelected) 10.sp else 9.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
