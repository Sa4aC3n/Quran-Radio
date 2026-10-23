package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.SpiritualReminderBank
import com.example.model.SpiritualReminderType
import com.example.services.AdhanNotificationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("إذاعات القرآن الكريم", appName)
  }

  @Test
  fun `spiritual reminders bank contains all key categories`() {
    val messages = SpiritualReminderBank.messages
    assertTrue(messages.isNotEmpty())
    assertTrue(messages.any { it.type == SpiritualReminderType.QURAN_RECITATION })
    assertTrue(messages.any { it.type == SpiritualReminderType.LIVE_RADIO })
    assertTrue(messages.any { it.type == SpiritualReminderType.TAFSIR_AND_TADABBUR })
    assertTrue(messages.any { it.type == SpiritualReminderType.GROUP_KHATMA })
  }

  @Test
  fun `reminder notification sound raw resource is present and accessible offline`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val inputStream = context.resources.openRawResource(R.raw.reminder_notification_sound)
    assertNotNull(inputStream)
    val availableBytes = inputStream.available()
    assertTrue("Audio sound resource should have valid byte size", availableBytes > 1000)
    inputStream.close()
  }

  @Test
  fun `all bundled adhan sounds have valid offline raw audio resources`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sounds = com.example.model.AdhanSound.entries
    assertTrue(sounds.isNotEmpty())

    sounds.forEach { sound ->
      val stream = context.resources.openRawResource(sound.rawResId)
      assertNotNull("Raw resource for ${sound.id} must be non-null", stream)
      val size = stream.available()
      assertTrue("Raw resource for ${sound.id} should have valid size", size > 10000)
      stream.close()

      if (sound.fajrRawResId != sound.rawResId) {
        val fajrStream = context.resources.openRawResource(sound.fajrRawResId)
        assertNotNull(fajrStream)
        assertTrue(fajrStream.available() > 10000)
        fajrStream.close()
      }
    }
  }
}
