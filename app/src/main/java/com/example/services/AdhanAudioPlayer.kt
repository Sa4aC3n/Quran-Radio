package com.example.services

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import com.example.model.AdhanSound
import com.example.model.PrayerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Universal Adhan audio player directly playing embedded high-quality compressed
 * offline audio resources (with full error recovery & synthesis fallback).
 */
class AdhanAudioPlayer(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var mediaPlayer: MediaPlayer? = null
    private var synthTrack: AudioTrack? = null
    private var playbackJob: Job? = null

    private val _isPlayingPreview = MutableStateFlow<String?>(null)
    val isPlayingPreview: StateFlow<String?> = _isPlayingPreview.asStateFlow()

    private val _isAdhanPlaying = MutableStateFlow(false)
    val isAdhanPlaying: StateFlow<Boolean> = _isAdhanPlaying.asStateFlow()

    fun playPreview(sound: AdhanSound) {
        if (_isPlayingPreview.value == sound.id) {
            stopPreview()
            return
        }

        stopPreview()
        _isPlayingPreview.value = sound.id

        startPlayback(sound.rawResId, isPreview = true) {
            _isPlayingPreview.value = null
        }
    }

    fun stopPreview() {
        stopPlayback()
        _isPlayingPreview.value = null
    }

    fun playFullAdhan(sound: AdhanSound, prayerType: PrayerType = PrayerType.FAJR, onCompletion: () -> Unit = {}) {
        stopPlayback()
        _isAdhanPlaying.value = true

        val resId = if (prayerType == PrayerType.FAJR) sound.fajrRawResId else sound.rawResId

        startPlayback(resId, isPreview = false) {
            _isAdhanPlaying.value = false
            onCompletion()
        }
    }

    fun stopAdhan() {
        stopPlayback()
        _isAdhanPlaying.value = false
        _isPlayingPreview.value = null
    }

    private fun startPlayback(rawResId: Int, isPreview: Boolean, onDone: () -> Unit) {
        try {
            stopPlayback()

            val afd: AssetFileDescriptor = appContext.resources.openRawResourceFd(rawResId)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(if (isPreview) AudioAttributes.USAGE_MEDIA else AudioAttributes.USAGE_ALARM)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        .build()
                )
                setWakeMode(appContext, android.os.PowerManager.PARTIAL_WAKE_LOCK)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()

                setOnPreparedListener { mp ->
                    mp.start()
                }
                setOnCompletionListener {
                    stopPlayback()
                    onDone()
                }
                setOnErrorListener { _, _, _ ->
                    playSynthesizedAdhan(isPreview, onDone)
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            playSynthesizedAdhan(isPreview, onDone)
        }
    }

    /**
     * Synthesize melodious harmonic Takbeerat tones completely offline as fallback.
     */
    private fun playSynthesizedAdhan(isPreview: Boolean, onDone: () -> Unit) {
        playbackJob?.cancel()
        playbackJob = scope.launch {
            try {
                val sampleRate = 44100
                val durationSec = if (isPreview) 6 else 20
                val totalSamples = sampleRate * durationSec
                val buffer = ShortArray(totalSamples)

                val notes = doubleArrayOf(330.0, 392.0, 440.0, 523.25, 440.0, 392.0)
                val noteDurationSamples = sampleRate * 1.5

                for (i in 0 until totalSamples) {
                    val noteIndex = ((i / noteDurationSamples).toInt()) % notes.size
                    val freq = notes[noteIndex]
                    val t = i.toDouble() / sampleRate

                    val wave1 = sin(2.0 * Math.PI * freq * t)
                    val wave2 = 0.5 * sin(2.0 * Math.PI * (freq * 2.0) * t)
                    val wave3 = 0.25 * sin(2.0 * Math.PI * (freq * 3.0) * t)
                    val envelope = (1.0 - (i % noteDurationSamples) / noteDurationSamples).coerceIn(0.0, 1.0)

                    val sampleVal = ((wave1 + wave2 + wave3) * envelope * 0.4 * Short.MAX_VALUE).toInt()
                    buffer[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                synthTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(if (isPreview) AudioAttributes.USAGE_MEDIA else AudioAttributes.USAGE_ALARM)
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
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                synthTrack?.write(buffer, 0, buffer.size)
                synthTrack?.play()

                kotlinx.coroutines.delay(durationSec * 1000L)
                stopPlayback()
                onDone()
            } catch (e: Exception) {
                e.printStackTrace()
                onDone()
            }
        }
    }

    private fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            synthTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) it.stop()
                it.release()
            }
            synthTrack = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
