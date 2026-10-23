package com.example.player

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Encodes raw 16-bit PCM audio slices into universal uncompressed RIFF/WAV files.
 * Zero external libraries, completely offline, zero compression loss, universally playable.
 */
object AudioClipExporter {

    fun exportToWav(
        pcmData: ByteArray,
        sampleRate: Int,
        channelCount: Int,
        outputFile: File
    ): Boolean {
        if (pcmData.isEmpty()) return false

        return try {
            outputFile.parentFile?.mkdirs()
            FileOutputStream(outputFile).use { fos ->
                val totalAudioLen = pcmData.size.toLong()
                val totalDataLen = totalAudioLen + 36
                val byteRate = (sampleRate * channelCount * 16 / 8).toLong()

                val header = ByteArray(44)
                val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

                // 1. RIFF Header
                buffer.put('R'.code.toByte())
                buffer.put('I'.code.toByte())
                buffer.put('F'.code.toByte())
                buffer.put('F'.code.toByte())
                buffer.putInt((totalDataLen and 0xffffffffL).toInt())
                buffer.put('W'.code.toByte())
                buffer.put('A'.code.toByte())
                buffer.put('V'.code.toByte())
                buffer.put('E'.code.toByte())

                // 2. Format Chunk
                buffer.put('f'.code.toByte())
                buffer.put('m'.code.toByte())
                buffer.put('t'.code.toByte())
                buffer.put(' '.code.toByte())
                buffer.putInt(16) // Subchunk1Size = 16 for PCM
                buffer.putShort(1.toShort()) // AudioFormat 1 = PCM (Linear quantization)
                buffer.putShort(channelCount.toShort())
                buffer.putInt(sampleRate)
                buffer.putInt(byteRate.toInt())
                buffer.putShort((channelCount * 16 / 8).toShort()) // BlockAlign
                buffer.putShort(16.toShort()) // BitsPerSample

                // 3. Data Chunk
                buffer.put('d'.code.toByte())
                buffer.put('a'.code.toByte())
                buffer.put('t'.code.toByte())
                buffer.put('a'.code.toByte())
                buffer.putInt((totalAudioLen and 0xffffffffL).toInt())

                fos.write(header)
                fos.write(pcmData)
                fos.flush()
            }

            outputFile.exists() && outputFile.length() > 44
        } catch (_: Exception) {
            outputFile.delete()
            false
        }
    }

    fun createClipFile(context: Context, stationName: String, seconds: Int): File {
        val clipsDir = File(context.cacheDir, "clips").apply { mkdirs() }
        val sanitized = stationName.replace("[^a-zA-Z0-9_\\u0600-\\u06FF]".toRegex(), "_").take(20)
        val fileName = "quran_clip_${sanitized}_${seconds}s_${System.currentTimeMillis()}.wav"
        return File(clipsDir, fileName)
    }
}
