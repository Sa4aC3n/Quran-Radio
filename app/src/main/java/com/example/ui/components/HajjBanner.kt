package com.example.ui.components

import android.content.Intent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IslamicSeasonEngine
import com.example.model.IslamicSeason
import com.example.model.IslamicSeasonState
import com.example.model.RadioStation
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LiveCoral
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBluePrimary

/**
 * High-craft Hajj Season & Day of Arafah Smart Banner.
 * Shows Hajj Rituals Guide, Arafah Golden Hour countdown,
 * Talbiyah live audio playback, and Eid celebration greetings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HajjBanner(
    seasonState: IslamicSeasonState,
    onPlayStation: (RadioStation) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isRitualSheetOpen by remember { mutableStateOf(false) }
    var isArafahDuaSheetOpen by remember { mutableStateOf(false) }
    var isEidSheetOpen by remember { mutableStateOf(false) }

    val ritual = seasonState.currentHajjRitual
    val isArafah = seasonState.isArafahActive
    val isEid = seasonState.isEidActive

    val infiniteTransition = rememberInfiniteTransition(label = "hajj_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hajj_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("hajj_smart_banner"),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = if (isArafah) {
                            listOf(Color(0xFF8C6614), Color(0xFF2C1E04))
                        } else if (isEid) {
                            listOf(Color(0xFF1B4938), Color(0xFF0D251D))
                        } else {
                            listOf(DeepEmerald.copy(alpha = 0.95f), Color(0xFF13382C))
                        }
                    )
                )
                .padding(16.dp)
        ) {
            // 1. Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isArafah) "🕋" else if (isEid) "🎉" else "🕋",
                            fontSize = 18.sp
                        )
                    }
                    Column {
                        Text(
                            text = when {
                                isArafah -> "يوم عرفة المبارك (الحج عرفة)"
                                isEid -> if (seasonState.season == IslamicSeason.EID_FITR) "عيد الفطر المبارك" else "عيد الأضحى المبارك"
                                else -> "موسم الحج وعشر ذي الحجة"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = seasonState.hijriDetails.formattedAr,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PureWhite.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                // Quick Talbiyah Stream Button
                Surface(
                    onClick = { onPlayStation(IslamicSeasonEngine.HAJJ_TALBIYAH_STATION) },
                    shape = RoundedCornerShape(12.dp),
                    color = GoldAccent.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, GoldAccent),
                    modifier = Modifier.testTag("talbiyah_stream_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "التلبية والتكبير",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Special Card: Arafah Live / Ritual Highlight
            if (isArafah) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onPlayStation(IslamicSeasonEngine.MAKKAH_LIVE_STATION) }
                        .testTag("arafah_live_card"),
                    shape = RoundedCornerShape(16.dp),
                    color = LiveCoral.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, GoldAccent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(LiveCoral)
                            )
                            Column {
                                Text(
                                    text = "🔴 بث مباشر خطبة عرفات والوقوف بصعيد عرفة",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite,
                                        fontSize = 13.sp
                                    )
                                )
                                Text(
                                    text = "استمع للخطبة ومناسك الحجاج مباشرة من صعيد عرفات الطاهر",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = PureWhite.copy(alpha = 0.85f),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GoldAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "استماع",
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            } else if (ritual != null) {
                // Hajj Daily Ritual Spotlight
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { isRitualSheetOpen = true }
                        .testTag("hajj_ritual_highlight_card"),
                    shape = RoundedCornerShape(16.dp),
                    color = PureWhite.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = ritual.dayNameAr,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                            Text(
                                text = ritual.locationAr,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = PureWhite.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = ritual.summaryAr,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PureWhite.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isArafah) {
                    // Arafah Dua Button
                    Surface(
                        onClick = { isArafahDuaSheetOpen = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = GoldAccent.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "🤲", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "دعاء يوم عرفة المستجاب",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                } else if (ritual != null) {
                    // Hajj Guide Button
                    Surface(
                        onClick = { isRitualSheetOpen = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = GoldAccent.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "دليل مناسك اليوم بالتفصيل",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Makkah Live Stream Broadcast
                Surface(
                    onClick = { onPlayStation(IslamicSeasonEngine.MAKKAH_LIVE_STATION) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = SkyBluePrimary.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, SkyBluePrimary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.Mosque, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "بث الحرم المكي الشريف",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }

    // Modal: Detailed Hajj Ritual Guide
    if (isRitualSheetOpen && ritual != null) {
        ModalBottomSheet(
            onDismissRequest = { isRitualSheetOpen = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "دليل مناسك ${ritual.dayNameAr}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GoldAccent)
                )
                Text(
                    text = "المكان: ${ritual.locationAr}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "شرح أعمال ومناسك اليوم:",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = ritual.detailsAr,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "الذكر والدعاء المأثور:",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = GoldAccent)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = ritual.keyDuaAr,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                lineHeight = 28.sp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "🕋 مناسك ${ritual.dayNameAr} (${ritual.locationAr}):\n\n${ritual.detailsAr}\n\n🤲 الدعاء: ${ritual.keyDuaAr}\n(من تطبيق إذاعات القرآن الكريم ومواقيت الصلاة)")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة الدليل"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاركة مناسك اليوم")
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Modal: Arafah Dua & Golden Hour Info
    if (isArafahDuaSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isArafahDuaSheetOpen = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "خير الدعاء دعاء يوم عرفة",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                )
                Text(
                    text = "قال رسول الله ﷺ: (خير الدعاء دعاء يوم عرفة، وخير ما قلت أنا والنبيون من قبلي...)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "لا إِلَهَ إِلا اللَّهُ وَحْدَهُ لا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ.",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 36.sp,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = GoldAccent.copy(alpha = 0.25f))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "💡 تنبيه: ساعة الاستجابة العظمى في يوم عرفة تبدأ من بعد صلاة العصر حتى غروب شمس يوم عرفة، فأكثروا من التضرع والدعاء والاستغفار.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "🤲 خير الدعاء دعاء يوم عرفة:\n\nلا إِلَهَ إِلا اللَّهُ وَحْدَهُ لا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ.\n\n(من تطبيق إذاعات القرآن الكريم ومواقيت الصلاة)")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة دعاء عرفة"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاركة دعاء يوم عرفة")
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
