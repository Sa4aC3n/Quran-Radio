package com.example

import com.example.player.AudioClipExporter
import com.example.player.CircularAudioBuffer
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RollingAudioBufferTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testCircularAudioBufferWriteAndExtract() {
        val buffer = CircularAudioBuffer(maxSeconds = 10)
        val sampleRate = 8000
        val channels = 1
        val bytesPerSecond = sampleRate * channels * 2 // 16000 bytes/sec

        buffer.ensureCapacity(sampleRate, channels)

        // Write 3 seconds of dummy audio
        val secondsToWrite = 3
        val totalBytes = secondsToWrite * bytesPerSecond
        val inputData = ByteArray(totalBytes) { (it % 128).toByte() }

        val byteBuffer = ByteBuffer.wrap(inputData)
        buffer.write(byteBuffer)

        val available = buffer.getAvailableSeconds(sampleRate, channels)
        assertEquals(3.0f, available, 0.05f)

        // Extract 2 seconds
        val extracted = buffer.extractLastSeconds(2, sampleRate, channels)
        val expectedLength = 2 * bytesPerSecond
        assertEquals(expectedLength, extracted.size)

        // Check content matches the last 2 seconds
        val expectedOffset = (3 - 2) * bytesPerSecond
        val expectedSlice = inputData.sliceArray(expectedOffset until totalBytes)
        assertArrayEquals(expectedSlice, extracted)
    }

    @Test
    fun testCircularAudioBufferWrapAround() {
        // 5 seconds capacity
        val buffer = CircularAudioBuffer(maxSeconds = 5)
        val sampleRate = 1000
        val channels = 1
        val bytesPerSecond = sampleRate * channels * 2 // 2000 bytes/sec
        val capacityBytes = 5 * bytesPerSecond // 10000 bytes

        buffer.ensureCapacity(sampleRate, channels)

        // Write 8 seconds of sequential data (wraps around 3 seconds)
        val totalBytes = 8 * bytesPerSecond // 16000 bytes
        val inputData = ByteArray(totalBytes) { (it and 0xFF).toByte() }

        buffer.write(ByteBuffer.wrap(inputData))

        // Available seconds should be capped at maxSeconds (5s)
        val available = buffer.getAvailableSeconds(sampleRate, channels)
        assertEquals(5.0f, available, 0.01f)

        // Extract the full 5 seconds
        val extracted = buffer.extractLastSeconds(5, sampleRate, channels)
        assertEquals(capacityBytes, extracted.size)

        // The extracted data should correspond to the last 5 seconds (bytes 6000 to 16000)
        val expectedStart = totalBytes - capacityBytes
        val expectedSlice = inputData.sliceArray(expectedStart until totalBytes)
        assertArrayEquals(expectedSlice, extracted)
    }

    @Test
    fun testCircularAudioBufferClear() {
        val buffer = CircularAudioBuffer(maxSeconds = 10)
        buffer.ensureCapacity(44100, 2)

        val dummyData = ByteArray(44100 * 4) // 1 second
        buffer.write(ByteBuffer.wrap(dummyData))
        assertTrue(buffer.getAvailableSeconds(44100, 2) > 0.9f)

        buffer.clear()
        assertEquals(0.0f, buffer.getAvailableSeconds(44100, 2), 0.001f)
        val extracted = buffer.extractLastSeconds(5, 44100, 2)
        assertEquals(0, extracted.size)
    }

    @Test
    fun testAudioClipExporterValidWavHeader() {
        val sampleRate = 44100
        val channels = 2
        val pcmSize = 44100 * channels * 2 // 1 second of audio
        val pcmData = ByteArray(pcmSize) { (it % 100).toByte() }

        val outputFile = File(tempFolder.root, "test_clip.wav")
        val success = AudioClipExporter.exportToWav(
            pcmData = pcmData,
            sampleRate = sampleRate,
            channelCount = channels,
            outputFile = outputFile
        )

        assertTrue(success)
        assertTrue(outputFile.exists())
        assertEquals(44L + pcmSize, outputFile.length())

        // Read and verify WAV Header
        val header = ByteArray(44)
        FileInputStream(outputFile).use { it.read(header) }
        val headerBuf = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

        // RIFF
        assertEquals('R'.code.toByte(), headerBuf.get())
        assertEquals('I'.code.toByte(), headerBuf.get())
        assertEquals('F'.code.toByte(), headerBuf.get())
        assertEquals('F'.code.toByte(), headerBuf.get())
        val chunkSize = headerBuf.getInt()
        assertEquals(pcmSize + 36, chunkSize)

        // WAVE
        assertEquals('W'.code.toByte(), headerBuf.get())
        assertEquals('A'.code.toByte(), headerBuf.get())
        assertEquals('V'.code.toByte(), headerBuf.get())
        assertEquals('E'.code.toByte(), headerBuf.get())

        // fmt
        assertEquals('f'.code.toByte(), headerBuf.get())
        assertEquals('m'.code.toByte(), headerBuf.get())
        assertEquals('t'.code.toByte(), headerBuf.get())
        assertEquals(' '.code.toByte(), headerBuf.get())
        val subchunk1Size = headerBuf.getInt()
        assertEquals(16, subchunk1Size)
        val audioFormat = headerBuf.getShort()
        assertEquals(1.toShort(), audioFormat) // PCM
        val numChannels = headerBuf.getShort()
        assertEquals(channels.toShort(), numChannels)
        val sr = headerBuf.getInt()
        assertEquals(sampleRate, sr)
        val byteRate = headerBuf.getInt()
        assertEquals(sampleRate * channels * 2, byteRate)
        val blockAlign = headerBuf.getShort()
        assertEquals((channels * 2).toShort(), blockAlign)
        val bitsPerSample = headerBuf.getShort()
        assertEquals(16.toShort(), bitsPerSample)

        // data
        assertEquals('d'.code.toByte(), headerBuf.get())
        assertEquals('a'.code.toByte(), headerBuf.get())
        assertEquals('t'.code.toByte(), headerBuf.get())
        assertEquals('a'.code.toByte(), headerBuf.get())
        val subchunk2Size = headerBuf.getInt()
        assertEquals(pcmSize, subchunk2Size)
    }

    @Test
    fun testAudioClipExporterEmptyDataReturnsFalse() {
        val outputFile = File(tempFolder.root, "empty.wav")
        val success = AudioClipExporter.exportToWav(
            pcmData = ByteArray(0),
            sampleRate = 44100,
            channelCount = 2,
            outputFile = outputFile
        )
        assertFalse(success)
        assertFalse(outputFile.exists())
    }
}
