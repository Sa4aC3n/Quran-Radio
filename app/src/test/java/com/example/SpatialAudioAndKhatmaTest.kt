package com.example

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import com.example.model.AcousticEnvironment
import com.example.model.AmbientSoundscape
import com.example.model.SpatialAudioConfig
import com.example.player.SpatialAcousticAudioProcessor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SpatialAudioAndKhatmaTest {

    @Test
    fun testSpatialAudioConfigDefaultsAndEnvironments() {
        val defaultConfig = SpatialAudioConfig()
        assertFalse(defaultConfig.isEnabled)
        assertEquals(AcousticEnvironment.ORIGINAL, defaultConfig.environment)
        assertEquals(AmbientSoundscape.NONE, defaultConfig.ambientSoundscape)

        val makkah = AcousticEnvironment.MAKKAH_HARAM
        assertEquals("رحاب الحرم المكي", makkah.titleAr)
        assertTrue(makkah.roomSize > 0.8f)

        val madinah = AcousticEnvironment.MADINAH_HARAM
        assertEquals("أروقة المسجد النبوي", madinah.titleAr)
        assertTrue(madinah.damping > 0.3f)

        val aqsa = AcousticEnvironment.AL_AQSA
        assertEquals("المسجد الأقصى المبارك", aqsa.titleAr)
        assertTrue(aqsa.stereoWidth > 1.2f)

        val rain = AmbientSoundscape.GENTLE_RAIN
        assertEquals("صوت المطر الرقيق", rain.titleAr)
    }

    @Test
    fun testSpatialAcousticAudioProcessorLifecycleAndBufferProcessing() {
        val processor = SpatialAcousticAudioProcessor()

        // 1. Initial State: inactive before configure
        assertFalse(processor.isActive)

        val inputFormat = AudioProcessor.AudioFormat(44100, 2, C.ENCODING_PCM_16BIT)
        val initialOutputFormat = processor.configure(inputFormat)
        assertTrue(processor.isActive)

        // 2. Enable processor with Makkah Haram preset
        processor.updateConfig(
            SpatialAudioConfig(
                isEnabled = true,
                environment = AcousticEnvironment.MAKKAH_HARAM,
                wetLevel = 0.35f
            )
        )
        val activeOutputFormat = processor.configure(inputFormat)
        assertTrue(processor.isActive)
        assertEquals(inputFormat.sampleRate, activeOutputFormat.sampleRate)
        assertEquals(inputFormat.channelCount, activeOutputFormat.channelCount)

        // 3. Feed 100 stereo PCM 16-bit samples (400 bytes)
        val numSamples = 100
        val inputBuffer = ByteBuffer.allocateDirect(numSamples * 2 * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until numSamples) {
            val sampleVal = ((i % 20) * 1000).toShort()
            inputBuffer.putShort(sampleVal)      // Left
            inputBuffer.putShort((-sampleVal).toShort()) // Right
        }
        inputBuffer.flip()

        // Process audio
        processor.queueInput(inputBuffer)

        // Verify output buffer is populated and contains valid data without overflow
        val outputBuffer = processor.output
        assertNotNull(outputBuffer)
        assertTrue(outputBuffer.remaining() > 0)

        // Read processed samples to ensure no NaN or corruption
        var maxSample = 0
        while (outputBuffer.hasRemaining()) {
            val sample = outputBuffer.short
            if (kotlin.math.abs(sample.toInt()) > maxSample) {
                maxSample = kotlin.math.abs(sample.toInt())
            }
        }
        assertTrue("Output buffer should have non-zero reverberated samples", maxSample > 0)

        // 4. Test Flush & Reset
        processor.flush()
        processor.reset()
        assertFalse(processor.isActive)
    }

    @Test
    fun testAcousticEnvironmentLookup() {
        assertEquals(AcousticEnvironment.MAKKAH_HARAM, AcousticEnvironment.fromId("makkah"))
        assertEquals(AcousticEnvironment.MADINAH_HARAM, AcousticEnvironment.fromId("madinah"))
        assertEquals(AcousticEnvironment.AL_AQSA, AcousticEnvironment.fromId("aqsa"))
        assertEquals(AcousticEnvironment.ORIGINAL, AcousticEnvironment.fromId("unknown_preset"))

        assertEquals(AmbientSoundscape.GENTLE_RAIN, AmbientSoundscape.fromId("rain"))
        assertEquals(AmbientSoundscape.DAWN_BREEZE, AmbientSoundscape.fromId("breeze"))
        assertEquals(AmbientSoundscape.NIGHT_CALM, AmbientSoundscape.fromId("night"))
        assertEquals(AmbientSoundscape.NONE, AmbientSoundscape.fromId("invalid"))
    }
}
