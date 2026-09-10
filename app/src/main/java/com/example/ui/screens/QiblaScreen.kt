package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QiblaInfo
import com.example.ui.components.LocationPickerDialog
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBluePrimary
import com.example.viewmodel.PrayerViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    viewModel: PrayerViewModel,
    modifier: Modifier = Modifier
) {
    val qiblaInfo by viewModel.qiblaState.collectAsState()
    val cityName by viewModel.cityName.collectAsState()
    val countryName by viewModel.countryName.collectAsState()
    val isLocating by viewModel.isLocating.collectAsState()
    val filteredCities by viewModel.filteredCities.collectAsState()
    val citySearchQuery by viewModel.citySearchQuery.collectAsState()

    var showLocationDialog by remember { mutableStateOf(false) }

    // Start/Stop sensors on screen lifecycle
    DisposableEffect(Unit) {
        viewModel.startQiblaSensors()
        onDispose {
            viewModel.stopQiblaSensors()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.refreshLocation()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "اتجاه القبلة المشرفة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "$cityName، $countryName",
                            style = MaterialTheme.typography.bodySmall,
                            color = SkyBluePrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showLocationDialog = true },
                        modifier = Modifier.testTag("btn_qibla_change_location")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "تغيير الموقع",
                            tint = SkyBluePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Alignment Status Card
            AlignmentStatusBanner(qiblaInfo = qiblaInfo)

            Spacer(modifier = Modifier.height(24.dp))

            // Interactive Compass
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                contentAlignment = Alignment.Center
            ) {
                val compassSize = minOf(maxWidth, 300.dp)
                QiblaCompassView(
                    qiblaInfo = qiblaInfo,
                    modifier = Modifier.size(compassSize)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Metrics Info Cards (Bearing & Distance to Makkah)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "زاوية القبلة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${qiblaInfo.qiblaBearing.toInt()}°",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = SkyBluePrimary
                        )
                        Text(
                            text = getCardinalDirectionAr(qiblaInfo.qiblaBearing),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "المسافة إلى الكعبة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${qiblaInfo.distanceKm.toInt()}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Text(
                            text = "كيلومتر",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calibration & Sensor Advisory
            SensorAdvisoryCard(
                accuracy = qiblaInfo.accuracy,
                isSensorAvailable = qiblaInfo.isSensorAvailable
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showLocationDialog) {
        LocationPickerDialog(
            cities = filteredCities,
            searchQuery = citySearchQuery,
            currentCityName = cityName,
            isLocating = isLocating,
            onSearchQueryChange = { viewModel.citySearchQuery.value = it },
            onSelectCity = { city ->
                viewModel.selectPresetCity(city)
                showLocationDialog = false
            },
            onUseGpsLocation = {
                if (viewModel.hasLocationPermission()) {
                    viewModel.refreshLocation()
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            onDismiss = { showLocationDialog = false }
        )
    }
}

@Composable
private fun AlignmentStatusBanner(qiblaInfo: QiblaInfo) {
    val isAligned = qiblaInfo.isAligned
    val diff = qiblaInfo.relativeAngle.toInt()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isAligned) Color(0xFF1B5E20) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("qibla_alignment_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isAligned) Icons.Default.CheckCircle else Icons.Default.Explore,
                contentDescription = null,
                tint = if (isAligned) GoldAccent else SkyBluePrimary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = if (isAligned) "أنت تواجه القبلة المشرفة تماماً 🕋" else "قم بتدوير الهاتف باتجاه السهم الذهبي",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isAligned) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isAligned) "جاهز للصلاة بإذن الله" else if (diff > 0) "تبقّى $diff° لليمين" else "تبقّى ${-diff}° لليسار",
                    fontSize = 12.sp,
                    color = if (isAligned) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QiblaCompassView(
    qiblaInfo: QiblaInfo,
    modifier: Modifier = Modifier
) {
    // Animate rotations smoothly
    val animatedAzimuth by animateFloatAsState(
        targetValue = -qiblaInfo.currentAzimuth,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "compass_azimuth"
    )

    val isAligned = qiblaInfo.isAligned
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aligned_glow"
    )

    Box(
        modifier = modifier
            .testTag("qibla_compass_canvas")
            .shadow(
                elevation = if (isAligned) 16.dp else 4.dp,
                shape = CircleShape,
                spotColor = if (isAligned) GoldAccent else SkyBluePrimary
            ),
        contentAlignment = Alignment.Center
    ) {
        // Outer Compass Ring & Dial (Rotates against phone heading)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(animatedAzimuth)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2 - 12.dp.toPx()

            // Outer decorative ring
            drawCircle(
                color = if (isAligned) GoldAccent.copy(alpha = pulseGlow) else Color(0xFF263238),
                radius = radius,
                center = center,
                style = Stroke(width = if (isAligned) 6.dp.toPx() else 3.dp.toPx())
            )

            // Inner disc
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A)),
                    center = center,
                    radius = radius
                ),
                radius = radius - 4.dp.toPx(),
                center = center
            )

            // Tick Marks
            for (i in 0 until 360 step 15) {
                val rad = Math.toRadians(i.toDouble() - 90)
                val isMajor = i % 90 == 0
                val tickLen = if (isMajor) 14.dp.toPx() else 8.dp.toPx()
                val tickColor = if (isMajor) SkyBluePrimary else Color.White.copy(alpha = 0.3f)
                val strokeW = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx()

                val startX = (center.x + (radius - tickLen) * cos(rad)).toFloat()
                val startY = (center.y + (radius - tickLen) * sin(rad)).toFloat()
                val endX = (center.x + radius * cos(rad)).toFloat()
                val endY = (center.y + radius * sin(rad)).toFloat()

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeW
                )
            }

            // North Pointer (Red)
            val northRad = Math.toRadians(-90.0)
            val northTip = Offset(
                (center.x + (radius - 20.dp.toPx()) * cos(northRad)).toFloat(),
                (center.y + (radius - 20.dp.toPx()) * sin(northRad)).toFloat()
            )
            drawCircle(color = Color(0xFFE53935), radius = 5.dp.toPx(), center = northTip)
        }

        // Qibla Indicator Needle pointing to Mecca Bearing
        val qiblaNeedleRotation = animatedAzimuth + qiblaInfo.qiblaBearing
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(qiblaNeedleRotation),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 - 20.dp.toPx()

                // Gold Needle toward Kaaba
                val needlePath = Path().apply {
                    moveTo(center.x, center.y - radius) // Tip
                    lineTo(center.x + 10.dp.toPx(), center.y)
                    lineTo(center.x, center.y + 16.dp.toPx())
                    lineTo(center.x - 10.dp.toPx(), center.y)
                    close()
                }

                drawPath(
                    path = needlePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(GoldAccent, Color(0xFFFFB300), Color(0xFFE65100))
                    ),
                    style = Fill
                )
            }

            // Kaaba icon badge at the needle tip
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldAccent),
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🕋", fontSize = 14.sp)
                    }
                }
            }
        }

        // Center Pivot Pin
        Surface(
            shape = CircleShape,
            color = if (isAligned) GoldAccent else SkyBluePrimary,
            border = androidx.compose.foundation.BorderStroke(3.dp, Color.White),
            modifier = Modifier.size(24.dp)
        ) {}
    }
}

@Composable
private fun SensorAdvisoryCard(
    accuracy: Int,
    isSensorAvailable: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (!isSensorAvailable) Icons.Default.Warning else Icons.Default.ScreenRotation,
                contentDescription = null,
                tint = if (!isSensorAvailable) Color(0xFFE53935) else SkyBluePrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (!isSensorAvailable) {
                        "جهازك لا يحتوي على حساس البوصلة المغناطيسية، تم حساب الاتجاه فلكياً بناءً على إحداثياتك."
                    } else if (accuracy <= 1) {
                        "دقة البوصلة منخفضة: حرّك هاتفك في الهواء على شكل الرقم (8) لمعايرة الحساس."
                    } else {
                        "ضع الهاتف في وضع أفقي مستوٍ وابتعد عن الأجهزة الإلكترونية والمعادن لدقة تامة."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getCardinalDirectionAr(bearing: Float): String {
    val b = (bearing % 360 + 360) % 360
    return when {
        b >= 337.5 || b < 22.5 -> "شمال"
        b >= 22.5 && b < 67.5 -> "شمال شرق"
        b >= 67.5 && b < 112.5 -> "شرق"
        b >= 112.5 && b < 157.5 -> "جنوب شرق"
        b >= 157.5 && b < 202.5 -> "جنوب"
        b >= 202.5 && b < 247.5 -> "جنوب غرب"
        b >= 247.5 && b < 292.5 -> "غرب"
        else -> "شمال غرب"
    }
}
