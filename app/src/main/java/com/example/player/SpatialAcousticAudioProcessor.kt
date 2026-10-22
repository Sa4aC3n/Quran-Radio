package com.example.player

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import androidx.media3.common.audio.AudioProcessor.UnhandledAudioFormatException
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import com.example.model.AcousticEnvironment
import com.example.model.SpatialAudioConfig
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min

/**
 * Real-time Schroeder/Freeverb DSP Spatial Acoustic AudioProcessor for Media3.
 * Simulates Islamic architectural acoustic spaces (Makkah Grand Mosque, Madinah Prophet's Mosque, Al-Aqsa).
 * Runs completely on-device without external dependencies or cloud latency.
 */
@OptIn(UnstableApi::class)
class SpatialAcousticAudioProcessor : BaseAudioProcessor() {

    @Volatile
    var config: SpatialAudioConfig = SpatialAudioConfig()
        set(value) {
            field = value
            updateReverbParameters(value)
        }

    fun updateConfig(newConfig: SpatialAudioConfig) {
        config = newConfig
    }

    // DSP Engine State
    private var isProcessorActive = false
    private var sampleRate = 44100
    private var channelCount = 2

    // Reverb Model Filters (Stereo Comb & Allpass arrays)
    private var leftCombFilters: Array<CombFilter>? = null
    private var rightCombFilters: Array<CombFilter>? = null
    private var leftAllpassFilters: Array<AllpassFilter>? = null
    private var rightAllpassFilters: Array<AllpassFilter>? = null

    // Base Delay Line Lengths at 44.1 kHz (Stereo spread)
    private val combTuningsLeft = intArrayOf(1116, 1188, 1277, 1356, 1422, 1491, 1557, 1617)
    private val combTuningsRight = intArrayOf(1139, 1211, 1300, 1379, 1445, 1514, 1580, 1640)
    private val allpassTuningsLeft = intArrayOf(556, 441, 341, 225)
    private val allpassTuningsRight = intArrayOf(579, 464, 364, 248)

    override fun onConfigure(inputAudioFormat: AudioFormat): AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            throw UnhandledAudioFormatException(inputAudioFormat)
        }
        if (inputAudioFormat.channelCount !in 1..2) {
            throw UnhandledAudioFormatException(inputAudioFormat)
        }

        sampleRate = inputAudioFormat.sampleRate
        channelCount = inputAudioFormat.channelCount

        // We output 16-bit PCM in the same channel count
        initFilters(sampleRate)
        updateReverbParameters(config)
        isProcessorActive = true

        return AudioFormat(sampleRate, channelCount, C.ENCODING_PCM_16BIT)
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        val activeConfig = config
        val env = activeConfig.environment

        // Fast Bypass if disabled or set to Original studio sound
        if (!activeConfig.isEnabled || env == AcousticEnvironment.ORIGINAL || activeConfig.wetLevel <= 0.001f) {
            val outputBuffer = replaceOutputBuffer(remaining)
            outputBuffer.put(inputBuffer)
            outputBuffer.flip()
            return
        }

        val lComb = leftCombFilters
        val rComb = rightCombFilters
        val lAllpass = leftAllpassFilters
        val rAllpass = rightAllpassFilters

        if (lComb == null || rComb == null || lAllpass == null || rAllpass == null) {
            val outputBuffer = replaceOutputBuffer(remaining)
            outputBuffer.put(inputBuffer)
            outputBuffer.flip()
            return
        }

        val wet = activeConfig.wetLevel.coerceIn(0.0f, 0.70f)
        val dry = 1.0f - (wet * 0.65f)
        val width = env.stereoWidth.coerceIn(0.5f, 2.0f)
        val wet1 = wet * (width / 2f + 0.5f)
        val wet2 = wet * ((1f - width) / 2f)

        val outputBuffer = replaceOutputBuffer(remaining)
        outputBuffer.order(ByteOrder.LITTLE_ENDIAN)

        if (channelCount == 2) {
            while (inputBuffer.remaining() >= 4) {
                val inL = inputBuffer.short.toFloat() / 32768.0f
                val inR = inputBuffer.short.toFloat() / 32768.0f

                var outL = 0f
                var outR = 0f

                // Parallel Comb filters
                for (i in lComb.indices) {
                    outL += lComb[i].process(inL)
                    outR += rComb[i].process(inR)
                }

                // Cascading Allpass filters
                for (i in lAllpass.indices) {
                    outL = lAllpass[i].process(outL)
                    outR = rAllpass[i].process(outR)
                }

                // Cross-mix and wet/dry blend
                val finalL = (inL * dry) + (outL * wet1) + (outR * wet2)
                val finalR = (inR * dry) + (outR * wet1) + (outL * wet2)

                // Hard clamp to 16-bit range
                val sampleL = (finalL * 32767.0f).toInt().coerceIn(-32768, 32767).toShort()
                val sampleR = (finalR * 32767.0f).toInt().coerceIn(-32768, 32767).toShort()

                outputBuffer.putShort(sampleL)
                outputBuffer.putShort(sampleR)
            }
        } else {
            // Mono stream: expand and process
            while (inputBuffer.remaining() >= 2) {
                val inM = inputBuffer.short.toFloat() / 32768.0f

                var outL = 0f
                for (i in lComb.indices) {
                    outL += lComb[i].process(inM)
                }
                for (i in lAllpass.indices) {
                    outL = lAllpass[i].process(outL)
                }

                val finalM = (inM * dry) + (outL * wet)
                val sampleM = (finalM * 32767.0f).toInt().coerceIn(-32768, 32767).toShort()
                outputBuffer.putShort(sampleM)
            }
        }

        outputBuffer.flip()
    }

    override fun onFlush() {
        leftCombFilters?.forEach { it.clear() }
        rightCombFilters?.forEach { it.clear() }
        leftAllpassFilters?.forEach { it.clear() }
        rightAllpassFilters?.forEach { it.clear() }
    }

    override fun onReset() {
        onFlush()
        leftCombFilters = null
        rightCombFilters = null
        leftAllpassFilters = null
        rightAllpassFilters = null
        isProcessorActive = false
    }

    override fun isActive(): Boolean = isProcessorActive

    private fun initFilters(rate: Int) {
        val rateFactor = rate.toFloat() / 44100.0f

        leftCombFilters = Array(combTuningsLeft.size) { i ->
            CombFilter(max(1, (combTuningsLeft[i] * rateFactor).toInt()))
        }
        rightCombFilters = Array(combTuningsRight.size) { i ->
            CombFilter(max(1, (combTuningsRight[i] * rateFactor).toInt()))
        }
        leftAllpassFilters = Array(allpassTuningsLeft.size) { i ->
            AllpassFilter(max(1, (allpassTuningsLeft[i] * rateFactor).toInt()))
        }
        rightAllpassFilters = Array(allpassTuningsRight.size) { i ->
            AllpassFilter(max(1, (allpassTuningsRight[i] * rateFactor).toInt()))
        }
    }

    private fun updateReverbParameters(cfg: SpatialAudioConfig) {
        val env = cfg.environment
        val feedback = env.roomSize.coerceIn(0.0f, 0.98f)
        val damp = env.damping.coerceIn(0.0f, 1.0f)

        leftCombFilters?.forEach {
            it.feedback = feedback
            it.damp = damp
        }
        rightCombFilters?.forEach {
            it.feedback = feedback
            it.damp = damp
        }
    }

    // DSP Building Blocks
    private class CombFilter(size: Int) {
        private val buffer = FloatArray(size)
        private var bufferIndex = 0
        private var filterStore = 0f
        var feedback = 0.5f
        var damp = 0.5f

        fun process(input: Float): Float {
            val output = buffer[bufferIndex]
            filterStore = (output * (1f - damp)) + (filterStore * damp)
            buffer[bufferIndex] = input + (filterStore * feedback)
            if (++bufferIndex >= buffer.size) bufferIndex = 0
            return output
        }

        fun clear() {
            buffer.fill(0f)
            filterStore = 0f
            bufferIndex = 0
        }
    }

    private class AllpassFilter(size: Int) {
        private val buffer = FloatArray(size)
        private var bufferIndex = 0
        var feedback = 0.5f

        fun process(input: Float): Float {
            val bufOut = buffer[bufferIndex]
            val output = -input + bufOut
            buffer[bufferIndex] = input + (bufOut * feedback)
            if (++bufferIndex >= buffer.size) bufferIndex = 0
            return output
        }

        fun clear() {
            buffer.fill(0f)
            bufferIndex = 0
        }
    }
}
