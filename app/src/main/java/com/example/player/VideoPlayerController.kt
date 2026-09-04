package com.example.player

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.audio.AmbientMusicManager
import com.example.audio.VoiceoverManager
import com.example.data.model.AspectRatio
import com.example.data.model.CameraMotion
import com.example.data.model.ProjectEntity
import com.example.data.model.SceneEntity
import com.example.data.model.SubtitlePosition
import com.example.data.model.SubtitleStyle
import com.example.data.model.VoiceLanguage
import com.example.data.model.VoiceTone
import com.example.subtitles.SrtManager
import com.example.subtitles.SubtitleCue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VideoPlayerController(
    private val context: Context,
    val voiceoverManager: VoiceoverManager,
    val ambientMusicManager: AmbientMusicManager,
    private val scope: CoroutineScope
) {
    var isPlaying by mutableStateOf(false)
        private set

    var currentMs by mutableLongStateOf(0L)
        private set

    var totalDurationMs by mutableLongStateOf(30000L)
        private set

    var currentSceneIndex by mutableIntStateOf(0)
        private set

    var sceneProgress by mutableFloatStateOf(0f)
        private set

    var activeSubtitleText by mutableStateOf("")
        private set

    var playbackSpeed by mutableFloatStateOf(1.0f)
        private set

    var isMuted by mutableStateOf(false)
        private set

    // Subtitle Customization
    var subtitleSizeSp by mutableIntStateOf(18)
    var subtitlePosition by mutableStateOf(SubtitlePosition.BOTTOM)
    var subtitleStyle by mutableStateOf(SubtitleStyle.YELLOW_BLACK)
    var subtitleEnabled by mutableStateOf(true)

    // Aspect Ratio
    var aspectRatio by mutableStateOf(AspectRatio.LANDSCAPE_16_9)

    private var scenes: List<SceneEntity> = emptyList()
    private var subtitleCues: List<SubtitleCue> = emptyList()
    private var playbackJob: Job? = null
    private var lastAnnouncedScene = -1

    fun loadProject(project: ProjectEntity, projectScenes: List<SceneEntity>) {
        pause()
        scenes = projectScenes
        totalDurationMs = scenes.sumOf { it.durationSeconds * 1000L }.coerceAtLeast(1000L)
        subtitleCues = SrtManager.parseSrt(project.srtContent)

        // Load project settings
        subtitleSizeSp = project.subtitleSizeSp
        subtitlePosition = try {
            SubtitlePosition.valueOf(project.subtitlePosition)
        } catch (e: Exception) {
            SubtitlePosition.BOTTOM
        }
        subtitleStyle = try {
            SubtitleStyle.valueOf(project.subtitleStyle)
        } catch (e: Exception) {
            SubtitleStyle.YELLOW_BLACK
        }
        subtitleEnabled = project.subtitleEnabled

        aspectRatio = try {
            AspectRatio.valueOf(project.aspectRatio)
        } catch (e: Exception) {
            AspectRatio.LANDSCAPE_16_9
        }

        val lang = try {
            VoiceLanguage.valueOf(project.language)
        } catch (e: Exception) {
            VoiceLanguage.HINDI
        }
        val tone = try {
            VoiceTone.valueOf(project.voiceTone)
        } catch (e: Exception) {
            VoiceTone.DEEP_NARRATOR
        }
        voiceoverManager.setVoiceConfig(lang, tone)

        seekTo(0L)
    }

    fun play() {
        if (isPlaying) return
        isPlaying = true
        lastAnnouncedScene = -1
        if (!isMuted) {
            ambientMusicManager.startMusic("Cinematic Ambient")
        }

        playbackJob = scope.launch(Dispatchers.Main) {
            val tickInterval = 50L
            while (isActive && isPlaying) {
                val step = (tickInterval * playbackSpeed).toLong()
                val nextMs = currentMs + step

                if (nextMs >= totalDurationMs) {
                    currentMs = totalDurationMs
                    updatePlaybackState()
                    pause()
                    break
                } else {
                    currentMs = nextMs
                    updatePlaybackState()
                }

                delay(tickInterval)
            }
        }
    }

    fun pause() {
        isPlaying = false
        playbackJob?.cancel()
        playbackJob = null
        voiceoverManager.stop()
        ambientMusicManager.stopMusic()
    }

    fun togglePlayPause() {
        if (isPlaying) pause() else play()
    }

    fun seekTo(ms: Long) {
        currentMs = ms.coerceIn(0L, totalDurationMs)
        updatePlaybackState()
        // If user sought to a new scene while playing, speak new scene
        if (isPlaying && currentSceneIndex != lastAnnouncedScene) {
            speakCurrentScene()
        }
    }

    fun nextScene() {
        if (scenes.isEmpty()) return
        var accum = 0L
        for (i in 0 until scenes.size) {
            val sceneEnd = accum + scenes[i].durationSeconds * 1000L
            if (accum > currentMs) {
                seekTo(accum)
                return
            }
            accum = sceneEnd
        }
        seekTo(totalDurationMs)
    }

    fun prevScene() {
        if (scenes.isEmpty()) return
        var accum = 0L
        var targetMs = 0L
        for (i in 0 until scenes.size) {
            val sceneStart = accum
            val sceneEnd = accum + scenes[i].durationSeconds * 1000L
            if (currentMs > sceneStart + 1000) {
                targetMs = sceneStart
            } else if (i > 0 && currentMs <= sceneStart + 1000) {
                targetMs = (sceneStart - (scenes[i - 1].durationSeconds * 1000L)).coerceAtLeast(0L)
            }
            accum = sceneEnd
        }
        seekTo(targetMs)
    }

    fun setSpeed(speed: Float) {
        playbackSpeed = speed
    }

    fun toggleMute() {
        isMuted = !isMuted
        if (isMuted) {
            voiceoverManager.stop()
            ambientMusicManager.stopMusic()
        } else if (isPlaying) {
            ambientMusicManager.startMusic("Cinematic Ambient")
            speakCurrentScene()
        }
    }

    private fun updatePlaybackState() {
        if (scenes.isEmpty()) {
            currentSceneIndex = 0
            sceneProgress = 0f
            activeSubtitleText = ""
            return
        }

        var accum = 0L
        var foundIndex = scenes.lastIndex
        var progressInScene = 0f

        for (i in scenes.indices) {
            val sceneDur = scenes[i].durationSeconds * 1000L
            if (currentMs < accum + sceneDur) {
                foundIndex = i
                val elapsedInScene = currentMs - accum
                progressInScene = (elapsedInScene.toFloat() / sceneDur.toFloat()).coerceIn(0f, 1f)
                break
            }
            accum += sceneDur
        }

        val sceneChanged = foundIndex != currentSceneIndex
        currentSceneIndex = foundIndex
        sceneProgress = progressInScene

        // Update Subtitles
        if (subtitleEnabled) {
            val activeCue = SrtManager.getActiveCue(subtitleCues, currentMs)
            activeSubtitleText = activeCue?.text ?: scenes.getOrNull(currentSceneIndex)?.subtitleText ?: ""
        } else {
            activeSubtitleText = ""
        }

        // Voiceover trigger on scene transition
        if (isPlaying && !isMuted && (sceneChanged || lastAnnouncedScene != foundIndex)) {
            speakCurrentScene()
        }
    }

    private fun speakCurrentScene() {
        val scene = scenes.getOrNull(currentSceneIndex) ?: return
        lastAnnouncedScene = currentSceneIndex
        if (scene.narrationText.isNotBlank()) {
            voiceoverManager.speak(scene.narrationText, "scene_${scene.id}_${scene.sceneIndex}")
        }
    }

    fun getCurrentScene(): SceneEntity? = scenes.getOrNull(currentSceneIndex)
}
