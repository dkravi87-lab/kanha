package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.VoiceLanguage
import com.example.data.model.VoiceTone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sin

class VoiceoverManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var currentLanguage = VoiceLanguage.HINDI
    private var currentPitch = 1.0f
    private var currentSpeed = 1.0f

    var onSpeakingStarted: (() -> Unit)? = null
    var onSpeakingDone: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            applyLanguage(currentLanguage)
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onSpeakingStarted?.invoke()
                }

                override fun onDone(utteranceId: String?) {
                    onSpeakingDone?.invoke()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    onSpeakingDone?.invoke()
                }
            })
        } else {
            Log.e("VoiceoverManager", "TTS initialization failed: $status")
        }
    }

    fun setVoiceConfig(language: VoiceLanguage, tone: VoiceTone, customPitch: Float? = null, customSpeed: Float? = null) {
        currentLanguage = language
        currentPitch = customPitch ?: tone.pitch
        currentSpeed = customSpeed ?: tone.speed

        if (isInitialized) {
            applyLanguage(language)
            tts?.setPitch(currentPitch)
            tts?.setSpeechRate(currentSpeed)
        }
    }

    private fun applyLanguage(language: VoiceLanguage) {
        val locale = when (language) {
            VoiceLanguage.HINDI -> Locale("hi", "IN")
            VoiceLanguage.ENGLISH -> Locale.US
            VoiceLanguage.BENGALI -> Locale("bn", "IN")
            VoiceLanguage.TAMIL -> Locale("ta", "IN")
            VoiceLanguage.TELUGU -> Locale("te", "IN")
            VoiceLanguage.SPANISH -> Locale("es", "ES")
            VoiceLanguage.FRENCH -> Locale.FRENCH
            VoiceLanguage.GERMAN -> Locale.GERMAN
            VoiceLanguage.JAPANESE -> Locale.JAPANESE
            VoiceLanguage.ARABIC -> Locale("ar", "SA")
        }

        try {
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to default if specific dialect is missing
                tts?.setLanguage(Locale.getDefault())
            }
        } catch (e: Exception) {
            Log.e("VoiceoverManager", "Error setting language", e)
        }
    }

    fun speak(text: String, utteranceId: String = "kishu_tts_${System.currentTimeMillis()}") {
        if (!isInitialized || text.isBlank()) return
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        tts?.setPitch(currentPitch)
        tts?.setSpeechRate(currentSpeed)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

/**
 * Procedural ambient soundtrack generator using AudioTrack.
 * Generates soft cinematic, sci-fi, or calm ambient pads without external audio files!
 */
class AmbientMusicManager {

    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun startMusic(mood: String = "Cinematic Ambient") {
        if (isPlaying) return
        isPlaying = true

        val sampleRate = 22050
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        try {
            audioTrack = AudioTrack.Builder()
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

            audioTrack?.play()

            synthJob = scope.launch {
                val baseFreq = when {
                    mood.contains("Epic", ignoreCase = true) -> 130.81 // C3
                    mood.contains("Sci-Fi", ignoreCase = true) || mood.contains("Cyberpunk", ignoreCase = true) -> 146.83 // D3
                    mood.contains("Cartoon", ignoreCase = true) -> 261.63 // C4
                    else -> 110.0 // A2
                }

                val buffer = ShortArray(1024)
                var phase1 = 0.0
                var phase2 = 0.0
                var time = 0.0

                while (isActive && isPlaying) {
                    val currentFreq = baseFreq + sin(time * 0.2) * 5.0
                    val harmonicFreq = currentFreq * 1.5 // fifth

                    for (i in buffer.indices) {
                        phase1 += 2.0 * Math.PI * currentFreq / sampleRate
                        phase2 += 2.0 * Math.PI * harmonicFreq / sampleRate

                        // Warm ambient pad with low volume
                        val sample = (sin(phase1) * 0.25 + sin(phase2) * 0.15) * 32767 * 0.12
                        buffer[i] = sample.toInt().coerceIn(-32768, 32767).toShort()
                        time += 1.0 / sampleRate
                    }
                    audioTrack?.write(buffer, 0, buffer.size)
                    delay(20)
                }
            }
        } catch (e: Exception) {
            Log.e("AmbientMusicManager", "Error generating ambient track", e)
        }
    }

    fun stopMusic() {
        isPlaying = false
        synthJob?.cancel()
        synthJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            Log.e("AmbientMusicManager", "Error stopping track", e)
        }
    }
}
