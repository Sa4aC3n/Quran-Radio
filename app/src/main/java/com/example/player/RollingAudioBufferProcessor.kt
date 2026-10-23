package com.example.player

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import androidx.media3.common.audio.AudioProcessor.UnhandledAudioFormatException
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer

/**
 * Media3 AudioProcessor that passively captures live decoded PCM audio into a
 * circular ring buffer (up to 90 seconds) without altering audio output or latency.
 */
@OptIn(UnstableApi::class)
class RollingAudioBufferProcessor(
    val circularBuffer: CircularAudioBuffer = CircularAudioBuffer(maxSeconds = 90)
) : BaseAudioProcessor() {

    var currentSampleRate: Int = 44100
        private set

    var currentChannelCount: Int = 2
        private set

    override fun onConfigure(inputAudioFormat: AudioFormat): AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            throw UnhandledAudioFormatException(inputAudioFormat)
        }
        currentSampleRate = inputAudioFormat.sampleRate
        currentChannelCount = inputAudioFormat.channelCount

        circularBuffer.ensureCapacity(currentSampleRate, currentChannelCount)

        return AudioFormat(currentSampleRate, currentChannelCount, C.ENCODING_PCM_16BIT)
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        // Capture a duplicate slice for the rolling buffer
        val duplicate = inputBuffer.asReadOnlyBuffer()
        circularBuffer.write(duplicate)

        // Transparent pass-through to output
        val outputBuffer = replaceOutputBuffer(remaining)
        outputBuffer.put(inputBuffer)
        outputBuffer.flip()
    }

    override fun onReset() {
        circularBuffer.clear()
    }

    fun getRecordedSeconds(): Float {
        return circularBuffer.getAvailableSeconds(currentSampleRate, currentChannelCount)
    }

    fun extractLastSeconds(seconds: Int): ByteArray {
        return circularBuffer.extractLastSeconds(seconds, currentSampleRate, currentChannelCount)
    }

    fun clearBuffer() {
        circularBuffer.clear()
    }
}
