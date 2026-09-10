package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdhanSound
import com.example.model.AsrJuristic
import com.example.model.CalculationMethod
import com.example.model.PrayerType
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SkyBluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhanSettingsBottomSheet(
    sheetState: SheetState,
    currentMethod: CalculationMethod,
    currentAsrJuristic: AsrJuristic,
    selectedAdhanSound: AdhanSound,
    previewPlayingSoundId: String?,
    minuteOffsets: Map<PrayerType, Int>,
    iqamahSettings: Map<PrayerType, Boolean>,
    iqamahDelays: Map<PrayerType, Int>,
    onSelectMethod: (CalculationMethod) -> Unit,
    onSelectAsrJuristic: (AsrJuristic) -> Unit,
    onSelectAdhanSound: (AdhanSound) -> Unit,
    onPlayPreviewSound: (AdhanSound) -> Unit,
    onStopPreviewSound: () -> Unit,
    onTestAdhanNotification: () -> Unit = {},
    onStopAdhanNotification: () -> Unit = {},
    onToggleIqamah: (PrayerType, Boolean) -> Unit,
    onSetIqamahDelay: (PrayerType, Int) -> Unit,
    onSetMinuteOffset: (PrayerType, Int) -> Unit,
    onOpenBatteryGuide: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = {
            onStopPreviewSound()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("adhan_settings_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = SkyBluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إعدادات الأذان ومواقيت الصلاة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                IconButton(onClick = {
                    onStopPreviewSound()
                    onDismiss()
                }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Intelligent Adhan & Radio Integration Info & Test Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "نظام الأذان وإيقاف الإذاعة تلقائياً 🕌",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "عند دخول وقت الصلاة، يتم إيقاف إذاعة القرآن مؤقتاً وتشغيل صوت الأذان النقي، وعند انتهاء الأذان تعود إذاعة القرآن للعمل تلقائياً وبكل سلاسة بدون إشعارات مزعجة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onTestAdhanNotification,
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تجربة تشغيل الأذان", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onStopAdhanNotification,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("إيقاف الصوت", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Battery Whitelist Promo Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryAlert,
                        contentDescription = null,
                        tint = SkyBluePrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "لضمان عمل الأذان بدون تأخير",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "استثنِ التطبيق من قيود توفير الطاقة والبطارية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onOpenBatteryGuide,
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("دليل الضبط", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 1: Adhan Sound Voice & Live Preview
            SectionTitle(icon = Icons.Default.VolumeUp, title = "صوت الأذان والمؤذن")

            AdhanSound.entries.forEach { sound ->
                val isSelected = sound.id == selectedAdhanSound.id
                val isPreviewing = previewPlayingSoundId == sound.id

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSelectAdhanSound(sound) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelectAdhanSound(sound) },
                            colors = RadioButtonDefaults.colors(selectedColor = SkyBluePrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sound.titleAr,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) SkyBluePrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = sound.muezzinAr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = DeepEmerald.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "مدمج بجودة عالية",
                                        color = DeepEmerald,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Preview button
                        IconButton(
                            onClick = {
                                if (isPreviewing) onStopPreviewSound() else onPlayPreviewSound(sound)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (isPreviewing) SkyBluePrimary else MaterialTheme.colorScheme.surface,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isPreviewing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "معاينة",
                                tint = if (isPreviewing) Color.White else SkyBluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Calculation Method
            SectionTitle(icon = Icons.Default.Calculate, title = "طريقة الحساب الفلكي")

            CalculationMethod.entries.forEach { method ->
                val isSelected = method.id == currentMethod.id
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable { onSelectMethod(method) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelectMethod(method) },
                            colors = RadioButtonDefaults.colors(selectedColor = SkyBluePrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = method.titleAr,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                color = if (isSelected) SkyBluePrimary else MaterialTheme.colorScheme.onSurface
                            )
                            val desc = if (method.ishaIntervalMinutes > 0) {
                                "الفجر: ${method.fajrAngle}° | العشاء: بعد المغرب بـ ${method.ishaIntervalMinutes} دقيقة"
                            } else {
                                "الفجر: ${method.fajrAngle}° | العشاء: ${method.ishaAngle}°"
                            }
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Asr Juristic Method
            SectionTitle(icon = Icons.Default.AccessTime, title = "طريقة حساب وقت صلاة العصر")

            AsrJuristic.entries.forEach { juristic ->
                val isSelected = juristic.id == currentAsrJuristic.id
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable { onSelectAsrJuristic(juristic) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelectAsrJuristic(juristic) },
                            colors = RadioButtonDefaults.colors(selectedColor = SkyBluePrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = juristic.titleAr,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = if (isSelected) SkyBluePrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 4: Iqamah Settings (Delays & Toggles)
            SectionTitle(icon = Icons.Default.NotificationsActive, title = "تنبيه وقت الإقامة في المسجد")

            listOf(PrayerType.FAJR, PrayerType.DHUHR, PrayerType.ASR, PrayerType.MAGHRIB, PrayerType.ISHA).forEach { prayer ->
                val isIqamahEnabled = iqamahSettings[prayer] ?: false
                val delayMin = iqamahDelays[prayer] ?: 15

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "إقامة صلاة ${prayer.arabicName}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(
                                text = if (isIqamahEnabled) "بعد الأذان بـ $delayMin دقيقة" else "التنبيه معطل",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isIqamahEnabled) SkyBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isIqamahEnabled) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onSetIqamahDelay(prayer, (delayMin - 5).coerceAtLeast(5)) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = "تقليل", modifier = Modifier.size(16.dp))
                                    }
                                    Text(
                                        text = "$delayMin د",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    IconButton(
                                        onClick = { onSetIqamahDelay(prayer, (delayMin + 5).coerceAtMost(60)) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "زيادة", modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            Switch(
                                checked = isIqamahEnabled,
                                onCheckedChange = { onToggleIqamah(prayer, it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = SkyBluePrimary)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 5: Manual Minute Offsets (+/-)
            SectionTitle(icon = Icons.Default.Tune, title = "الضبط اليدوي لدقائق الصلوات (+/-)")

            PrayerType.entries.forEach { prayer ->
                val offset = minuteOffsets[prayer] ?: 0
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = prayer.arabicName, fontWeight = FontWeight.Medium, fontSize = 14.sp)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onSetMinuteOffset(prayer, (offset - 1).coerceAtLeast(-30)) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = "تقليل", modifier = Modifier.size(16.dp))
                            }

                            val offsetText = if (offset > 0) "+$offset دقيقة" else if (offset < 0) "$offset دقيقة" else "0 دقيقة"
                            Text(
                                text = offsetText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (offset != 0) SkyBluePrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            IconButton(
                                onClick = { onSetMinuteOffset(prayer, (offset + 1).coerceAtMost(30)) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "زيادة", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SectionTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SkyBluePrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
