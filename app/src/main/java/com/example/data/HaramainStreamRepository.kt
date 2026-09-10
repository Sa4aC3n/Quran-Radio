package com.example.data

import android.util.Log
import com.example.model.HaramainChannelId
import com.example.model.HaramainLiveChannel

class HaramainStreamRepository {

    companion object {
        private const val TAG = "HARAMAIN"
    }

    private val makkahChannel = HaramainLiveChannel(
        id = HaramainChannelId.MAKKAH,
        nameAr = "الحرم المكي مباشر",
        subtitleAr = "بث مباشر من المسجد الحرام",
        sourcePageUrl = "https://aloula.sba.sa/live/quran",
        primaryStreamUrl = "https://win.holol.com/live/quran/playlist.m3u8",
        fallbackStreamUrls = listOf(
            "https://cdn-globecast.akamaized.net/live/eds/saudi_quran/hls_roku/index.m3u8",
            "http://m.live.net.sa:1935/live/quran/playlist.m3u8"
        ),
        streamType = "HLS / M3U8",
        isVerified = true,
        description = "البث المباشر الرسمي لقناة القرآن الكريم من رحاب مكة المكرمة والحرم المكي الشريف"
    )

    private val madinahChannel = HaramainLiveChannel(
        id = HaramainChannelId.MADINAH,
        nameAr = "المسجد النبوي مباشر",
        subtitleAr = "بث مباشر من الحرم المدني",
        sourcePageUrl = "https://aloula.sba.sa/live/sunna",
        primaryStreamUrl = "https://win.holol.com/live/sunnah/playlist.m3u8",
        fallbackStreamUrls = listOf(
            "https://cdn-globecast.akamaized.net/live/eds/saudi_sunnah/hls_roku/index.m3u8",
            "http://m.live.net.sa:1935/live/sunnah/playlist.m3u8"
        ),
        streamType = "HLS / M3U8",
        isVerified = true,
        description = "البث المباشر الرسمي لقناة السنة النبوية من رحاب المدينة المنورة والمسجد النبوي الشريف"
    )

    fun getChannels(): List<HaramainLiveChannel> {
        return listOf(makkahChannel, madinahChannel)
    }

    fun getChannel(id: HaramainChannelId): HaramainLiveChannel {
        return when (id) {
            HaramainChannelId.MAKKAH -> {
                Log.d(TAG, "Fetching Makkah stream: ${makkahChannel.sourcePageUrl}")
                makkahChannel
            }
            HaramainChannelId.MADINAH -> {
                Log.d(TAG, "Fetching Madinah stream: ${madinahChannel.sourcePageUrl}")
                madinahChannel
            }
        }
    }
}
