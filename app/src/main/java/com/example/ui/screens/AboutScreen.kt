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
import com.example.ui.theme.NavyMuted
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PureWhite
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

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "حول التطبيق",
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
                                contentDescription = "رجوع",
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
                                text = "إذاعات القرآن الكريم",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SkyBluePrimary,
                                    fontSize = 17.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "استمع إلى القرآن الكريم من إذاعات العالم",
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
                                    text = "الإصدار ${BuildConfig.VERSION_NAME}",
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
                        text = "عن التطبيق",
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
                                    text = "رسالة إذاعات القرآن الكريم",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "إذاعات القرآن الكريم هو تطبيق يهدف إلى جمع محطات القرآن الكريم والبث القرآني من مختلف الدول في مكان واحد، ليمنحك تجربة استماع سهلة وهادئة أينما كنت.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 24.sp,
                                    fontSize = 14.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "نسعى إلى تقديم تجربة بسيطة وموثوقة لمحبي القرآن الكريم، مع إمكانية الوصول إلى الإذاعات والمحطات القرآنية والاستماع إليها مباشرة عبر الإنترنت.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 24.sp,
                                    fontSize = 13.5.sp
                                )
                            )
                        }
                    }



                    Spacer(modifier = Modifier.height(16.dp))

                    // Section: Interaction & Developer Contact
                    Text(
                        text = "التواصل والمشاركة",
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
                            .testTag("about_contact_share_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // 1. Contact Us (Email)
                            ActionRowItem(
                                icon = Icons.Default.Email,
                                title = "تواصل معنا",
                                subtitle = "m.k3shka@gmail.com",
                                isSubtitleLtr = true,
                                testTag = "btn_contact_email",
                                onClick = {
                                    sendEmail(
                                        context = context,
                                        email = "m.k3shka@gmail.com",
                                        subject = "Quran Radio World - تواصل معنا",
                                        onError = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("لا يوجد تطبيق بريد إلكتروني مثبت على الجهاز.")
                                            }
                                        }
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 2. Share App
                            ActionRowItem(
                                icon = Icons.Default.Share,
                                title = "شارك التطبيق",
                                subtitle = "شارك الأجر ومرر التطبيق إلى أصدقائك وعائلتك",
                                testTag = "btn_share_app",
                                onClick = {
                                    shareApp(
                                        context = context,
                                        text = "استمع إلى القرآن الكريم من إذاعات العالم عبر تطبيق Quran Radio World ❤️"
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 3. Rate App
                            ActionRowItem(
                                icon = Icons.Default.Star,
                                title = "قيّم التطبيق",
                                subtitle = "دعمك لنا يساعد في نشر تطبيق القرآن الكريم",
                                testTag = "btn_rate_app",
                                onClick = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("شكرًا لاهتمامك ودعمك لتطبيق إذاعات القرآن الكريم ❤️")
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section: Legal Information
                    Text(
                        text = "المعلومات القانونية",
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
                            .testTag("about_legal_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            ActionRowItem(
                                icon = Icons.Default.PrivacyTip,
                                title = "سياسة الخصوصية",
                                subtitle = "التطبيق يحترم خصوصيتك بالكامل ولا يجمع أي بيانات شخصية",
                                testTag = "btn_privacy_policy",
                                onClick = {
                                    activeDialogTitle = "سياسة الخصوصية"
                                    activeDialogContent = "تطبيق Quran Radio World (إذاعات القرآن الكريم) يحترم خصوصية المستخدمين بشكل كامل.\n\n• لا يتم جمع أو تتبع أي بيانات شخصية أو موقع جغرافي.\n• جميع التفضيلات كالمحطات المفضلة ومؤقت النوم يتم تخزينها محليًا فقط على جهازك.\n• التطبيق يستخدم الاتصال بالإنترنت حصريًا لتشغيل روابط البث الإذاعي المباشر."
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            ActionRowItem(
                                icon = Icons.Default.Gavel,
                                title = "شروط الاستخدام",
                                subtitle = "تطبيق مجاني غير ربحي مخصص للاستماع لتلاوات القرآن الكريم",
                                testTag = "btn_terms_of_service",
                                onClick = {
                                    activeDialogTitle = "شروط الاستخدام"
                                    activeDialogContent = "تطبيق Quran Radio World مخصص لتمكين المسلمين ومحبي القرآن الكريم حول العالم من الاستماع إلى البث المباشر لإذاعات القرآن الرسمية.\n\n• جميع حقوق التلاوات والمحتوى الإذاعي محفوظة للإذاعات والهيئات الرسمية المصدرة لها.\n• استخدام التطبيق مجاني بالكامل."
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Elegant Footer
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("about_footer"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Quran Radio World",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "إذاعات القرآن الكريم",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SkyBluePrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "صنع بكل حـــــــ ❣️ ــــــــب في مصر",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = "m.k3shka@gmail.com",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SkyBluePrimary,
                                    fontSize = 11.5.sp
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        sendEmail(
                                            context = context,
                                            email = "m.k3shka@gmail.com",
                                            subject = "Quran Radio World - تواصل معنا",
                                            onError = {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("لا يوجد تطبيق بريد إلكتروني مثبت على الجهاز.")
                                                }
                                            }
                                        )
                                    }
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "© 2026 Quran Radio World",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "جميع الحقوق محفوظة",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        )
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
                            text = "حسناً",
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

private fun sendEmail(
    context: Context,
    email: String,
    subject: String,
    onError: () -> Unit
) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        onError()
    } catch (e: Exception) {
        onError()
    }
}

private fun shareApp(
    context: Context,
    text: String
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
