package com.example.magica.audio

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object DspEffects {

    fun applyVolume(samples: ShortArray, volume: Float): ShortArray {
        if (volume >= 0.999f) return samples
        val out = ShortArray(samples.size)
        for (i in samples.indices) {
            out[i] = (samples[i] * volume).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    fun applyPitchShift(samples: ShortArray, semitones: Float, sampleRate: Int): ShortArray {
        if (kotlin.math.abs(semitones) < 0.1f) return samples
        val ratio = 2.0.pow(semitones / 12.0)
        val newSize = (samples.size / ratio).toInt()
        val out = ShortArray(newSize)

        for (i in 0 until newSize) {
            val srcPos = i * ratio
            val srcIndex = srcPos.toInt()
            val frac = srcPos - srcIndex

            if (srcIndex + 1 < samples.size) {
                val a = samples[srcIndex].toInt()
                val b = samples[srcIndex + 1].toInt()
                val interpolated = a + ((b - a) * frac).toInt()
                out[i] = interpolated.coerceIn(-32768, 32767).toShort()
            } else if (srcIndex < samples.size) {
                out[i] = samples[srcIndex]
            }
        }
        return out
    }

    fun applySpeedChange(samples: ShortArray, speed: Float): ShortArray {
        if (kotlin.math.abs(speed - 1.0f) < 0.01f) return samples
        val newSize = (samples.size / speed).toInt()
        val out = ShortArray(newSize)

        for (i in 0 until newSize) {
            val srcPos = i * speed
            val srcIndex = srcPos.toInt()
            val frac = srcPos - srcIndex

            if (srcIndex + 1 < samples.size) {
                val a = samples[srcIndex].toInt()
                val b = samples[srcIndex + 1].toInt()
                val interpolated = a + ((b - a) * frac).toInt()
                out[i] = interpolated.coerceIn(-32768, 32767).toShort()
            } else if (srcIndex < samples.size) {
                out[i] = samples[srcIndex]
            }
        }
        return out
    }

    fun applyEqualizer(samples: ShortArray, bands: List<Float>, frequencies: List<Float>, sampleRate: Int): ShortArray {
        val hasEq = bands.any { kotlin.math.abs(it) > 0.5f }
        if (!hasEq) return samples

        val totalGain = 10f.pow(bands.sum() / (bands.size * 20f))
        val out = ShortArray(samples.size) { samples[it] }

        for ((i, gain) in bands.withIndex()) {
            if (kotlin.math.abs(gain) < 0.5f) continue
            val freq = frequencies[i]
            val dbGain = gain
            val filter = BiquadFilter()
            filter.calcPeakingEQ(sampleRate, freq, 1.0, dbGain)
            for (j in out.indices) {
                out[j] = filter.process(out[j])
            }
        }

        if (kotlin.math.abs(totalGain - 1.0f) > 0.01f && totalGain > 0f) {
            val invGain = 1f / totalGain
            for (i in out.indices) {
                out[i] = (out[i] * invGain).toInt().coerceIn(-32768, 32767).toShort()
            }
        }
        return out
    }

    fun applyReverb(samples: ShortArray, mix: Float, roomSize: Float, damping: Float, sampleRate: Int): ShortArray {
        if (mix < 0.01f) return samples

        val combDelays = intArrayOf(
            (sampleRate * 0.0297f).toInt(),
            (sampleRate * 0.0371f).toInt(),
            (sampleRate * 0.0411f).toInt(),
            (sampleRate * 0.0437f).toInt()
        )
        val combDecays = floatArrayOf(0.7f, 0.66f, 0.62f, 0.58f)
        val allpassDelays = intArrayOf(
            (sampleRate * 0.005f).toInt(),
            (sampleRate * 0.0017f).toInt()
        )
        val allpassDecays = floatArrayOf(0.5f, 0.5f)

        val scale = (1.0f - damping) * roomSize * mix
        val wet = FloatArray(samples.size)
        val dry = 1.0f - mix * 0.5f

        for (i in 0 until 4) {
            val delay = (combDelays[i] * roomSize).toInt().coerceAtLeast(1)
            val decay = combDecays[i]
            val buf = FloatArray(delay)
            var idx = 0
            for (j in samples.indices) {
                val input = samples[j] / 32768f
                val tmp = buf[idx]
                val output = input + tmp * decay * scale
                buf[idx] = output
                idx = (idx + 1) % delay
                wet[j] += tmp
            }
        }

        for (i in 0 until 2) {
            val delay = allpassDelays[i]
            val decay = allpassDecays[i]
            val buf = FloatArray(delay)
            var idx = 0
            for (j in wet.indices) {
                val input = wet[j] / 4f
                val tmp = buf[idx]
                val output = -input + tmp * decay
                buf[idx] = input + tmp * decay
                idx = (idx + 1) % delay
                wet[j] = output
            }
        }

        val out = ShortArray(samples.size)
        for (i in samples.indices) {
            val mixed = (samples[i] / 32768f) * dry + wet[i] * mix
            out[i] = (mixed * 32768f).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    fun applyChorus(samples: ShortArray, mix: Float, sampleRate: Int): ShortArray {
        if (mix < 0.01f) return samples
        val delayMs = 30f
        val depthMs = 10f
        val rate = 0.5f
        val delaySamples = (delayMs * sampleRate / 1000f).toInt()
        val depthSamples = (depthMs * sampleRate / 1000f).toInt()

        val maxDelay = delaySamples + depthSamples + 1
        val buf = FloatArray(maxDelay)
        var idx = 0
        var phase = 0f
        val out = ShortArray(samples.size)

        for (i in samples.indices) {
            val input = samples[i] / 32768f
            buf[idx] = input

            phase += rate * 2f * PI.toFloat() / sampleRate
            val modulation = sin(phase) * depthSamples
            val readIdx = ((idx - delaySamples - modulation.toInt()) % maxDelay + maxDelay) % maxDelay
            val delayed = buf[readIdx.toInt()]

            val mixed = input * (1f - mix) + delayed * mix
            out[i] = (mixed * 32768f).toInt().coerceIn(-32768, 32767).toShort()
            idx = (idx + 1) % maxDelay
        }
        return out
    }

    fun applyDistortion(samples: ShortArray, gain: Float): ShortArray {
        if (gain < 0.01f) return samples
        val drive = 1f + gain * 20f
        val out = ShortArray(samples.size)
        for (i in samples.indices) {
            var x = samples[i] / 32768f
            x *= drive
            x = (x / (1f + kotlin.math.abs(x))) * 1.5f
            x = x.coerceIn(-1f, 1f)
            out[i] = (x * 32768f).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    fun applyRobotEffect(samples: ShortArray, sampleRate: Int): ShortArray {
        val out = ShortArray(samples.size)
        val modFreq = 6f
        val depth = 0.3f
        for (i in samples.indices) {
            val t = i.toFloat() / sampleRate
            val mod = 1.0f + sin(t * modFreq * 2f * PI.toFloat()) * depth
            val srcIdx = (i.toFloat() / mod).toInt().coerceIn(0, samples.size - 1)
            out[i] = samples[srcIdx]
        }
        return out
    }

    fun applyLowPass(samples: ShortArray, cutoff: Float, sampleRate: Int): ShortArray {
        if (cutoff >= 20000f) return samples
        val filter = BiquadFilter()
        filter.calcLowPass(sampleRate, cutoff, 0.707f)
        val out = ShortArray(samples.size)
        for (i in samples.indices) {
            out[i] = filter.process(samples[i])
        }
        return out
    }

    fun applyHighPass(samples: ShortArray, cutoff: Float, sampleRate: Int): ShortArray {
        if (cutoff <= 20f) return samples
        val filter = BiquadFilter()
        filter.calcHighPass(sampleRate, cutoff, 0.707f)
        val out = ShortArray(samples.size)
        for (i in samples.indices) {
            out[i] = filter.process(samples[i])
        }
        return out
    }

    class BiquadFilter {
        private var a0 = 0.0
        private var a1 = 0.0
        private var a2 = 0.0
        private var b1 = 0.0
        private var b2 = 0.0
        private var x1 = 0.0
        private var x2 = 0.0
        private var y1 = 0.0
        private var y2 = 0.0

        fun process(input: Short): Short {
            val x = input.toDouble() / 32768.0
            val y = a0 * x + a1 * x1 + a2 * x2 - b1 * y1 - b2 * y2
            x2 = x1
            x1 = x
            y2 = y1
            y1 = y
            return (y * 32768.0).toInt().coerceIn(-32768, 32767).toShort()
        }

        fun calcPeakingEQ(sampleRate: Int, freq: Float, q: Double, dbGain: Float) {
            val a = 10.0.pow(dbGain / 40.0)
            val omega = 2.0 * PI * freq / sampleRate
            val alpha = sin(omega) / (2.0 * q)
            val cosOmega = cos(omega)

            b0 = 1.0 + alpha * a
            b1 = -2.0 * cosOmega
            b2 = 1.0 - alpha * a
            a0 = 1.0 + alpha / a
            a1 = -2.0 * cosOmega
            a2 = 1.0 - alpha / a

            val norm = 1.0 / a0
            a0 = b0 * norm
            a1 = b1 * norm
            a2 = b2 * norm
            b1 = a1 * norm
            b2 = a2 * norm
        }

        fun calcLowPass(sampleRate: Int, cutoff: Float, q: Double) {
            val omega = 2.0 * PI * cutoff / sampleRate
            val alpha = sin(omega) / (2.0 * q)
            val cosOmega = cos(omega)

            b0 = (1.0 - cosOmega) / 2.0
            b1 = 1.0 - cosOmega
            b2 = (1.0 - cosOmega) / 2.0
            a0 = 1.0 + alpha
            a1 = -2.0 * cosOmega
            a2 = 1.0 - alpha

            val norm = 1.0 / a0
            a0 = b0 * norm
            a1 = b1 * norm
            a2 = b2 * norm
            b1 = a1 * norm
            b2 = a2 * norm
        }

        fun calcHighPass(sampleRate: Int, cutoff: Float, q: Double) {
            val omega = 2.0 * PI * cutoff / sampleRate
            val alpha = sin(omega) / (2.0 * q)
            val cosOmega = cos(omega)

            b0 = (1.0 + cosOmega) / 2.0
            b1 = -(1.0 + cosOmega)
            b2 = (1.0 + cosOmega) / 2.0
            a0 = 1.0 + alpha
            a1 = -2.0 * cosOmega
            a2 = 1.0 - alpha

            val norm = 1.0 / a0
            a0 = b0 * norm
            a1 = b1 * norm
            a2 = b2 * norm
            b1 = a1 * norm
            b2 = a2 * norm
        }
    }
}
