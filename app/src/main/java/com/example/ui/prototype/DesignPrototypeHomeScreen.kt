package com.example.ui.prototype

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Isolated visual prototype for the Quran Radio homepage.
 *
 * This composable is intentionally not connected to MainActivity or the app's navigation.
 * All actions are placeholders; integrate only after visual review and explicit approval.
 */
private val Sky = Color(0xFF2796D0)
private val DeepBlue = Color(0xFF07518A)
private val InkBlue = Color(0xFF123F6E)
private val MutedBlue = Color(0xFF6385A2)
private val PaleBlue = Color(0xFFE7F6FD)
private val LineBlue = Color(0xFFDCECF5)

private data class PrototypeCategory(val symbol: String, val title: String, val subtitle: String)

private val prototypeCategories = listOf(
    PrototypeCategory("◉", "إذاعات الدول", "من مختلف دول العالم"),
    PrototypeCategory("♙", "إذاعات القراء", "أشهر القراء"),
    PrototypeCategory("▣", "إذاعات رسمية", "هيئات الإذاعة"),
    PrototypeCategory("۞", "تلاوات خاشعة", "تأخذك إلى عالم آخر"),
    PrototypeCategory("▤", "تفسير القرآن", "تأملات مميزة"),
    PrototypeCategory("文", "ترجمات المعاني", "بأكثر من لغة"),
    PrototypeCategory("۩", "السنة النبوية", "شرح وبيان"),
    PrototypeCategory("◆", "السيرة والقصص", "عبر ودروس"),
    PrototypeCategory("⚖", "الفتاوى والأحكام", "من علماء موثوقين"),
    PrototypeCategory("☼", "أذكار الصبح والمساء", "راحة وطمأنينة"),
    PrototypeCategory("♧", "الرقية الشرعية", "للاطمئنان والشفاء"),
    PrototypeCategory("◉", "القبلة", "اتجاهك في أي مكان")
)

@Composable
fun DesignPrototypeHomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3FAFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color(0xFF1684C3), Color(0xFF4BA9DE), Color(0xFF2489C8))))
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("۞", color = Color.White, fontSize = 38.sp)
                Column(Modifier.weight(1f)) {
                    Text("إذاعات القرآن الكريم", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                    Text("صوت القرآن.. يملأ حياتك نورًا", color = Color.White.copy(alpha = .9f), fontSize = 11.sp)
                }
                Text("⚙", color = Color.White, fontSize = 25.sp)
            }
            Spacer(Modifier.height(13.dp))
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = Color.White.copy(alpha = .88f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⌕", color = Sky, fontSize = 27.sp)
                    Spacer(Modifier.width(10.dp))
                    Text("ابحث عن إذاعة أو قارئ...", color = MutedBlue, fontSize = 14.sp)
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp)
        ) {
            HeroCard()
            CategorySection()
            PrayerSection()
            KhatmaSection()
            QuickShortcuts()
        }

        BottomNavigationPrototype()
    }
}

@Composable
private fun HeroCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 13.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF70C6ED), Color(0xFFE2F5FF), Color(0xFFF0FAFF))))
            .border(1.dp, Color(0xFFC8E9F9), RoundedCornerShape(20.dp))
            .padding(17.dp)
    ) {
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
            Surface(color = Color(0xFF0874B6), shape = RoundedCornerShape(22.dp)) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("▣  الإذاعة الرئيسية", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(11.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("إذاعة القرآن الكريم", color = DeepBlue, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.End)
                    Text("من القاهرة", color = DeepBlue, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(4.dp))
                    Text("الهيئة العامة للإذاعة والتلفزيون", color = InkBlue, fontSize = 12.sp)
                }
                Text("۞", color = Sky, fontSize = 52.sp)
            }
            Spacer(Modifier.height(13.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = CircleShape, color = Sky, modifier = Modifier.size(55.dp).border(3.dp, Color.White, CircleShape)) {
                    Box(contentAlignment = Alignment.Center) { Text("▶", color = Color.White, fontSize = 25.sp) }
                }
                Text("▮▮▮", color = Color(0xFF147DBB), fontSize = 17.sp)
                Surface(shape = RoundedCornerShape(22.dp), color = Color.White.copy(alpha = .8f), modifier = Modifier.border(1.dp, Color(0xFFA8D5E9), RoundedCornerShape(22.dp))) {
                    Row(Modifier.padding(horizontal = 11.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("●", color = Color(0xFF2BBF8A), fontSize = 9.sp)
                        Spacer(Modifier.width(5.dp))
                        Text("جاهز للاستماع", color = InkBlue, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySection() {
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
        val columns = if (maxWidth >= 600.dp) 4 else 2
        val gap = 9.dp
        val cardWidth = (maxWidth - gap * (columns - 1)) / columns
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            prototypeCategories.chunked(columns).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    rowItems.forEach { category ->
                        Card(
                            modifier = Modifier.width(cardWidth).aspectRatio(if (columns == 4) 1.28f else 1.14f).clickable { },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(
                                Modifier.fillMaxSize().padding(horizontal = 5.dp, vertical = 9.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    Modifier.size(if (columns == 4) 57.dp else 53.dp).clip(CircleShape)
                                        .background(Brush.radialGradient(listOf(Color.White, Color(0xFFD6F0FC), Color(0xFFC3E7F7)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(category.symbol, color = Color(0xFF0C70AE), fontSize = 29.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(category.title, color = DeepBlue, fontSize = if (columns == 4) 14.sp else 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2, lineHeight = 18.sp)
                                Spacer(Modifier.height(2.dp))
                                Text(category.subtitle, color = MutedBlue, fontSize = 10.sp, textAlign = TextAlign.Center, maxLines = 1)
                            }
                        }
                    }
                    repeat(columns - rowItems.size) { Spacer(Modifier.width(cardWidth)) }
                }
            }
        }
    }
}

@Composable
private fun PrayerSection() {
    Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        PrayerCardPrototype(
            title = "مواقيت الصلاة",
            subtitle = "وأذان الصلاة",
            button = "اعرض المواقيت  ‹",
            background = Brush.horizontalGradient(listOf(Color(0xFF1A72AE), Color(0xFF4BA6D9))),
            foreground = Color.White,
            symbol = "◷"
        )
        PrayerCardPrototype(
            title = "منبه الأذان",
            subtitle = "لا تفوّت الصلاة أبدًا",
            button = "إعداد المنبه  ‹",
            background = Brush.horizontalGradient(listOf(Color(0xFFD6F0FC), Color(0xFFF4FBFF))),
            foreground = DeepBlue,
            symbol = "♧"
        )
    }
}

@Composable
private fun PrayerCardPrototype(title: String, subtitle: String, button: String, background: Brush, foreground: Color, symbol: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(background).border(1.dp, Color(0xFFCBE5F4), RoundedCornerShape(17.dp)).padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(symbol, color = foreground.copy(alpha = .7f), fontSize = 36.sp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(title, color = foreground, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, color = foreground.copy(alpha = .86f), fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(24.dp), color = foreground.copy(alpha = .09f), modifier = Modifier.border(1.dp, foreground.copy(alpha = .4f), RoundedCornerShape(24.dp))) {
                Text(button, color = foreground, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
            }
        }
    }
}

@Composable
private fun KhatmaSection() {
    Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("۞  الختمات", color = InkBlue, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Text("عرض الكل  ‹", color = Sky, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        KhatmaCardPrototype("ختمة جماعية", "مع مجموعة من القراء", true)
        Spacer(Modifier.height(9.dp))
        KhatmaCardPrototype("ختمة خاصة", "اختر القارئ والمدة التي تناسبك", false)
    }
}

@Composable
private fun KhatmaCardPrototype(title: String, subtitle: String, dark: Boolean) {
    val background = if (dark) Brush.horizontalGradient(listOf(Color(0xFF123C78), Color(0xFF397AB5))) else Brush.horizontalGradient(listOf(Color(0xFFD4F0FF), Color(0xFFF7FCFF)))
    val foreground = if (dark) Color.White else DeepBlue
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(background).border(1.dp, Color(0xFFD0E7F5), RoundedCornerShape(16.dp)).padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(57.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = .22f)), contentAlignment = Alignment.Center) {
            Text(if (dark) "۞" else "▤", color = foreground, fontSize = 34.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(title, color = foreground, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, color = foreground.copy(alpha = .86f), fontSize = 11.sp, textAlign = TextAlign.End)
        }
        Text("‹", color = foreground, fontSize = 25.sp)
    }
}

@Composable
private fun QuickShortcuts() {
    Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp)) {
        Text("اختصارات سريعة", color = InkBlue, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(9.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("文" to "تغيير اللغة", "☾" to "رمضان والحج", "۩" to "الآن يُقرأ في الحرم", "◷" to "مؤقت النوم", "▣" to "بث مكة والمدينة").forEach { (symbol, title) ->
                Column(
                    Modifier.width(92.dp).clip(RoundedCornerShape(14.dp)).background(Color.White).border(1.dp, LineBlue, RoundedCornerShape(14.dp)).padding(horizontal = 5.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(Modifier.size(39.dp).clip(CircleShape).background(PaleBlue), contentAlignment = Alignment.Center) {
                        Text(symbol, color = Sky, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(title, color = InkBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 13.sp, maxLines = 2)
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationPrototype() {
    Surface(color = Color.White.copy(alpha = .97f), shadowElevation = 12.dp, shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            listOf("▤" to "القرآن", "▣" to "الإذاعات", "⌂" to "الرئيسية", "♡" to "المفضلة", "•••" to "المزيد").forEachIndexed { index, item ->
                Column(Modifier.weight(1f).clickable { }.padding(vertical = 3.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(item.first, color = if (index == 2) Sky else MutedBlue, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    Text(item.second, color = if (index == 2) Sky else MutedBlue, fontSize = 10.sp, fontWeight = if (index == 2) FontWeight.Bold else FontWeight.Medium)
                    if (index == 2) Spacer(Modifier.padding(top = 3.dp).width(30.dp).height(3.dp).clip(RoundedCornerShape(4.dp)).background(Sky))
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852, locale = "ar")
@Composable
private fun DesignPrototypeHomeScreenPreview() {
    MaterialTheme { DesignPrototypeHomeScreen() }
}
