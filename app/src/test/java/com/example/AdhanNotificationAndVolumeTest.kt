package com.example

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import com.example.model.PrayerType
import com.example.services.AdhanNotificationHelper
import com.example.services.AdhanVolumeKeyDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AdhanNotificationAndVolumeTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun testAdhanArtworkBitmapGenerated() {
        val bitmap = AdhanNotificationHelper.getAdhanArtworkBitmap(context, PrayerType.MAGHRIB)
        assertNotNull(bitmap)
        assertEquals(256, bitmap.width)
        assertEquals(256, bitmap.height)
    }

    @Test
    fun testBuildAdhanNotificationHasMediaStyleAndStopAction() {
        val notification = AdhanNotificationHelper.buildAdhanNotification(
            context = context,
            prayerType = PrayerType.MAGHRIB,
            cityName = "مكة المكرمة",
            muezzinName = "أذان الحرم المكي"
        )

        assertNotNull(notification)
        assertEquals(AdhanNotificationHelper.CHANNEL_ADHAN_ID, notification.channelId)

        // Must be ongoing during playback
        assertTrue((notification.flags and Notification.FLAG_ONGOING_EVENT) != 0)

        // Must have at least 1 action: the Stop button
        assertNotNull(notification.actions)
        assertTrue(notification.actions.isNotEmpty())

        val stopAction = notification.actions[0]
        assertTrue(stopAction.title.toString().contains("إيقاف الأذان"))
        assertNotNull(stopAction.actionIntent)
    }

    @Test
    fun testAdhanVolumeKeyDetectorDoublePressTriggersCallback() {
        val detector = AdhanVolumeKeyDetector(context)
        var doublePressTriggered = false

        detector.startListening {
            doublePressTriggered = true
        }

        // First press
        val firstResult = detector.onKeyEvent()
        assertFalse(firstResult)
        assertFalse(doublePressTriggered)

        // Simulate user waiting 250ms and pressing volume button a second time
        Thread.sleep(250)

        // Second press (within 150..1200ms)
        val secondResult = detector.onKeyEvent()
        assertTrue(secondResult)
        assertTrue(doublePressTriggered)

        // Further press after double press should not trigger again
        val thirdResult = detector.onKeyEvent()
        assertFalse(thirdResult)
    }

    @Test
    fun testAdhanVolumeKeyDetectorDebounceRejectsFastSpuriousEvents() {
        val detector = AdhanVolumeKeyDetector(context)
        var triggered = false

        detector.startListening {
            triggered = true
        }

        detector.onKeyEvent()

        // Immediate callback (< 120ms from a single physical button bounce)
        val debounceResult = detector.onKeyEvent()
        assertFalse(debounceResult)
        assertFalse(triggered)

        detector.stopListening()
    }
}
