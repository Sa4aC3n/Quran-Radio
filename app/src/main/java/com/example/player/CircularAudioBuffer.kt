package com.example.player

import java.nio.ByteBuffer
import kotlin.math.min

/**
 * Thread-safe Circular Audio Ring Buffer for continuous rolling radio recording.
 * Retains up to maxSeconds (default 90s) of raw PCM audio in memory.
 */
class CircularAudioBuffer(val maxSeconds: Int = 90) {

    private var buffer: ByteArray? = null
    private var capacityBytes: Int = 0
    private var writeHead: Int = 0
    private var totalBytesWritten: Long = 0L

    @Synchronized
    fun ensureCapacity(sampleRate: Int, channelCount: Int) {
        val bytesPerSecond = sampleRate * channelCount * 2 // 16-bit PCM
        val requiredCapacity = bytesPerSecond * maxSeconds
        if (buffer == null || capacityBytes != requiredCapacity) {
            capacityBytes = requiredCapacity
            buffer = ByteArray(requiredCapacity)
            writeHead = 0
            totalBytesWritten = 0L
        }
    }

    @Synchronized
    fun write(input: ByteBuffer) {
        val byteBuf = buffer ?: return
        val remaining = input.remaining()
        if (remaining == 0 || capacityBytes == 0) return

        val tempArray = ByteArray(min(remaining, 4096))
        while (input.hasRemaining()) {
            val toRead = min(input.remaining(), tempArray.size)
            input.get(tempArray, 0, toRead)

            for (i in 0 until toRead) {
                byteBuf[writeHead] = tempArray[i]
                writeHead = (writeHead + 1) % capacityBytes
            }
            totalBytesWritten += toRead
        }
    }

    @Synchronized
    fun getAvailableSeconds(sampleRate: Int, channelCount: Int): Float {
        val bytesPerSecond = sampleRate * channelCount * 2
        if (bytesPerSecond <= 0) return 0f
        val currentStoredBytes = min(totalBytesWritten, capacityBytes.toLong())
        return currentStoredBytes.toFloat() / bytesPerSecond.toFloat()
    }

    @Synchronized
    fun extractLastSeconds(seconds: Int, sampleRate: Int, channelCount: Int): ByteArray {
        val byteBuf = buffer ?: return ByteArray(0)
        val bytesPerSecond = sampleRate * channelCount * 2
        val requestedBytes = (seconds * bytesPerSecond).coerceAtMost(capacityBytes)
        val currentStoredBytes = min(totalBytesWritten, capacityBytes.toLong()).toInt()
        val bytesToCopy = min(requestedBytes, currentStoredBytes)

        if (bytesToCopy <= 0) return ByteArray(0)

        val output = ByteArray(bytesToCopy)
        var readIndex = (writeHead - bytesToCopy + capacityBytes) % capacityBytes

        for (i in 0 until bytesToCopy) {
            output[i] = byteBuf[readIndex]
            readIndex = (readIndex + 1) % capacityBytes
        }

        return output
    }

    @Synchronized
    fun clear() {
        writeHead = 0
        totalBytesWritten = 0L
    }
}
