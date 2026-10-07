package com.example.minuteman.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class RetroSoundEngine(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private var isMuted = false
    private var volume = 0.5f
    private val sampleRate = 22050

    fun isMuted(): Boolean = isMuted

    fun setMuted(muted: Boolean) {
        this.isMuted = muted
    }

    fun getVolume(): Float = volume

    fun setVolume(vol: Float) {
        this.volume = vol.coerceIn(0f, 1f)
    }

    fun playTone(
        freq: Float,
        type: String = "square",
        duration: Float = 0.08f,
        vol: Float = 0.25f,
        glideToFreq: Float? = null
    ) {
        if (isMuted || volume <= 0f) return

        scope.launch {
            try {
                val numSamples = (sampleRate * duration).toInt()
                if (numSamples <= 0) return@launch
                val buffer = ShortArray(numSamples)
                val targetVol = vol * volume * Short.MAX_VALUE

                var currentFreq = freq
                val freqStep = if (glideToFreq != null && numSamples > 1) {
                    (glideToFreq - freq) / numSamples
                } else 0f

                var phase = 0.0
                for (i in 0 until numSamples) {
                    // Envelope: fast attack, exponential decay
                    val progress = i.toFloat() / numSamples
                    val decay = (1f - progress) * (1f - progress)
                    val sampleAmp = targetVol * decay

                    val sampleVal = when (type.lowercase()) {
                        "triangle" -> {
                            val p = (phase / (2 * PI)) % 1.0
                            if (p < 0.5) (p * 4.0 - 1.0) else (3.0 - p * 4.0)
                        }
                        "sawtooth" -> {
                            val p = (phase / (2 * PI)) % 1.0
                            (p * 2.0 - 1.0)
                        }
                        "noise" -> {
                            (Random.nextFloat() * 2f - 1f).toDouble()
                        }
                        else -> { // "square"
                            if (sin(phase) >= 0) 1.0 else -1.0
                        }
                    }

                    buffer[i] = (sampleVal * sampleAmp).toInt().toShort()

                    phase += 2.0 * PI * currentFreq / sampleRate
                    currentFreq += freqStep
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(numSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, numSamples)
                audioTrack.play()
                kotlinx.coroutines.delay((duration * 1000).toLong() + 20)
                audioTrack.release()
            } catch (_: Exception) {
                // Ignore audio playback exceptions
            }
        }
    }

    fun menuMove() {
        playTone(740f, "square", 0.03f, 0.15f)
    }

    fun menuSelect() {
        playTone(520f, "square", 0.06f, 0.2f, 880f)
    }

    fun menuBack() {
        playTone(420f, "triangle", 0.08f, 0.18f, 220f)
    }

    fun beep(pitch: Float = 440f) {
        playTone(pitch, "square", 0.05f, 0.2f)
    }

    fun laser() {
        playTone(980f, "square", 0.09f, 0.22f, 120f)
    }

    fun jump() {
        playTone(150f, "triangle", 0.14f, 0.25f, 520f)
    }

    fun hit() {
        playTone(180f, "noise", 0.12f, 0.35f, 40f)
    }

    fun coin() {
        playTone(987f, "square", 0.06f, 0.22f)
        scope.launch {
            kotlinx.coroutines.delay(60)
            playTone(1318f, "square", 0.12f, 0.25f)
        }
    }

    fun powerup() {
        val notes = listOf(330f, 440f, 554f, 659f)
        notes.forEachIndexed { idx, freq ->
            scope.launch {
                kotlinx.coroutines.delay((idx * 60).toLong())
                playTone(freq, "square", 0.07f, 0.22f)
            }
        }
    }
}
