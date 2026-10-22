package com.example.player

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import com.example.model.AmbientSoundscape
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.sin

/**
 * Real-time procedural ambient sound synthesizer.
 * Generates continuous natural soundscapes (Rain, Breeze, Night Calm) in pure mathematics.
 * Requires 0 network, 0 external assets, and generates seamless non-looping audio.
 */
class AmbientSoundscapePlayer {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var synthesisJob: Job? = null
    private var audioTrack: AudioTrack? = null

    @Volatile
    private var currentSoundscape: AmbientSoundscape = AmbientSoundscape.NONE

    @Volatile
    private var currentVolume: Float = 0.08f

    @Volatile
    private var isRadioPlaying: Boolean = false

    private val sampleRate = 22050 // Lightweight 22.05 kHz mono for extreme battery efficiency
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(sampleRate / 4)

    fun updateConfig(soundscape: AmbientSoundscape, volume: Float) {
        currentSoundscape = soundscape
        currentVolume = volume.coerceIn(0.0f, 0.35f)
        evaluatePlaybackState()
    }

    fun onRadioPlaybackStateChanged(isPlaying: Boolean) {
        isRadioPlaying = isPlaying
        evaluatePlaybackState()
    }

    private fun evaluatePlaybackState() = synchronized(this) {
        val shouldPlay = isRadioPlaying && currentSoundscape != AmbientSoundscape.NONE && currentVolume > 0.001f

        if (shouldPlay) {
            if (synthesisJob == null || !synthesisJob!!.isActive) {
                startSynthesis()
            }
        } else {
            stopSynthesis()
        }
    }

    private fun startSynthesis() {
        stopSynthesis()

        try {
            audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STREAM
                )
            }

            audioTrack?.play()

            synthesisJob = scope.launch {
                val chunkSize = 1024
                val pcmBuffer = ShortArray(chunkSize)
                val random = Random()

                // DSP filter state
                var b0 = 0.0
                var b1 = 0.0
                var b2 = 0.0
                var lfoPhase = 0.0
                var brownState = 0.0

                while (isActive) {
                    val soundscape = currentSoundscape
                    val vol = currentVolume

                    if (soundscape == AmbientSoundscape.NONE || !isRadioPlaying) {
                        break
                    }

                    when (soundscape) {
                        AmbientSoundscape.GENTLE_RAIN -> {
                            // Pink noise with occasional raindrop transients
                            for (i in 0 until chunkSize) {
                                val white = (random.nextDouble() * 2.0 - 1.0)
                                b0 = 0.99765 * b0 + white * 0.0990460
                                b1 = 0.96300 * b1 + white * 0.1600000
                                b2 = 0.57000 * b2 + white * 0.8665000
                                var pink = b0 + b1 + b2 + white * 0.5362

                                // Random gentle micro-droplets
                                if (random.nextInt(320) == 0) {
                                    pink += (random.nextDouble() - 0.5) * 4.0
                                }

                                val sample = (pink * 0.18 * vol * 32767.0).toInt().coerceIn(-32768, 32767)
                                pcmBuffer[i] = sample.toShort()
                            }
                        }

                        AmbientSoundscape.DAWN_BREEZE -> {
                            // Brownian noise modulated with smooth wind gusts
                            for (i in 0 until chunkSize) {
                                val white = (random.nextDouble() * 2.0 - 1.0)
                                brownState = (brownState + (0.02 * white)) / 1.02
                                lfoPhase += 0.00035 // ~0.12 Hz breeze swell
                                val windGain = (sin(lfoPhase) * 0.4 + 0.6)

                                val sample = (brownState * 1.5 * windGain * vol * 32767.0).toInt().coerceIn(-32768, 32767)
                                pcmBuffer[i] = sample.toShort()
                            }
                        }

                        AmbientSoundscape.NIGHT_CALM -> {
                            // Ultra-warm low-pass night atmospheric hum
                            for (i in 0 until chunkSize) {
                                val white = (random.nextDouble() * 2.0 - 1.0)
                                brownState = (brownState + (0.008 * white)) / 1.008
                                lfoPhase += 0.00018
                                val pad = sin(lfoPhase * 3.5) * 0.15

                                val sample = ((brownState * 1.2 + pad) * vol * 32767.0).toInt().coerceIn(-32768, 32767)
                                pcmBuffer[i] = sample.toShort()
                            }
                        }

                        AmbientSoundscape.NONE -> {
                            pcmBuffer.fill(0)
                        }
                    }

                    audioTrack?.write(pcmBuffer, 0, chunkSize)
                }
            }
        } catch (e: Exception) {
            Log.w("AmbientSoundscape", "Failed to start ambient synthesizer: ${e.message}")
        }
    }

    private fun stopSynthesis() {
        synthesisJob?.cancel()
        synthesisJob = null

        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }

    fun release() {
        stopSynthesis()
    }
}
