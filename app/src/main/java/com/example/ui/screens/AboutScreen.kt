package com.example.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.localization.StringKey
import com.example.localization.appString
import com.example.ui.theme.NavyMuted
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBluePrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isVisible by remember { mutableStateOf(false) }
    var activeDialogTitle by remember { mutableStateOf<String?>(null) }
    var activeDialogContent by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = appString(StringKey.ABOUT_TITLE),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("about_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = appString(StringKey.NAV_BACK),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.testTag("about_snackbar_host")
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(400)) + slideInVertically(
                    animationSpec = tween(400),
                    initialOffsetY = { 50 }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("about_screen_content"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Hero Card (App Identity & Logo)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("about_hero_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Logo Container
                            Box(
                                modifier = Modifier
                                    .size(86.dp)
                                    .clip(RoundedCornerShape(26.dp))
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                SkyBluePrimary,
                                                MaterialTheme.colorScheme.primaryContainer
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Radio,
                                    contentDescription = "Quran Radio World Logo",
                                    tint = PureWhite,
                                    modifier = Modifier.size(44.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Quran Radio World",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 22.sp,
                                    letterSpacing = 0.5.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = appString(StringKey.APP_TITLE),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SkyBluePrimary,
                                    fontSize = 17.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = appString(StringKey.APP_SUBTITLE),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Version Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(1.dp, SkyBluePrimary.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "${appString(StringKey.ABOUT_VERSION)} ${BuildConfig.VERSION_NAME}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = SkyBluePrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section: About App (نبذة عن التطبيق)
                    Text(
                        text = appString(StringKey.ABOUT_MISSION_TITLE),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SkyBluePrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 6.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("about_description_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = SkyBluePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = appString(StringKey.ABOUT_MISSION_TITLE),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = appString(StringKey.ABOUT_MISSION_P1),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 24.sp,
                                    fontSize = 14.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = appString(StringKey.ABOUT_MISSION_P2),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 24.sp,
                                    fontSize = 13.5.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section: Developer Contact
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = SkyBluePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "تواصل مع فريق التطوير",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "نسعد دائماً بمقترحاتكم واستفساراتكم لتطوير التطبيق وخدمة كتاب الله.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.5.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }

        // Info / Terms Dialog
        if (activeDialogTitle != null && activeDialogContent != null) {
            AlertDialog(
                onDismissRequest = {
                    activeDialogTitle = null
                    activeDialogContent = null
                },
                title = {
                    Text(
                        text = activeDialogTitle ?: "",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Text(
                        text = activeDialogContent ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            activeDialogTitle = null
                            activeDialogContent = null
                        }
                    ) {
                        Text(
                            text = appString(StringKey.DIALOG_OK),
                            fontWeight = FontWeight.Bold,
                            color = SkyBluePrimary
                        )
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }

@Composable
private fun ActionRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit,
    isSubtitleLtr: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 6.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = SkyBluePrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            if (isSubtitleLtr) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                }
            } else {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp
                    )
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = null,
            tint = NavyMuted
        )
    }
}

private fun shareApp(
    context: Context,
    text: String = "استمع إلى القرآن الكريم من إذاعات العالم عبر تطبيق Quran Radio World ❤️\nhttps://play.google.com/store/apps/details?id=com.aistudio.quranradioworld.live"
) {
    try {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة التطبيق")
        context.startActivity(shareIntent)
    } catch (_: Exception) {}
}

private fun openPlayStore(context: Context) {
    val packageName = "com.aistudio.quranradioworld.live"
    try {
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(marketIntent)
    } catch (e: Exception) {
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        } catch (_: Exception) {}
    }
}
