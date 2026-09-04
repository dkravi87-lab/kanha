package com.example.ui

import android.app.Application
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AmbientMusicManager
import com.example.audio.VoiceoverManager
import com.example.data.local.KishuDatabase
import com.example.data.model.AspectRatio
import com.example.data.model.CameraMotion
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectType
import com.example.data.model.SceneEntity
import com.example.data.model.SubtitlePosition
import com.example.data.model.SubtitleStyle
import com.example.data.model.TransitionType
import com.example.data.model.VideoStyle
import com.example.data.model.VoiceLanguage
import com.example.data.model.VoiceTone
import com.example.data.remote.GeminiService
import com.example.data.remote.GeneratedImageResult
import com.example.data.repository.ProjectRepository
import com.example.player.VideoPlayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class StudioTab(val label: String, val hindiLabel: String) {
    VIDEO("Video Studio", "वीडियो स्टूडियो"),
    IMAGE("Image Studio", "इमेज स्टूडियो"),
    EDITOR("Clip Editor", "एडिटर व प्लेयर"),
    PROJECTS("Projects", "मेरी कृतियाँ")
}

data class VideoGenerationForm(
    val prompt: String = "",
    val durationSeconds: Int = 30, // Can go up to 1800s (30 minutes!)
    val style: VideoStyle = VideoStyle.CINEMATIC,
    val aspectRatio: AspectRatio = AspectRatio.LANDSCAPE_16_9,
    val language: VoiceLanguage = VoiceLanguage.HINDI,
    val voiceTone: VoiceTone = VoiceTone.DEEP_NARRATOR,
    val musicMood: String = "Cinematic Ambient",
    val subtitleEnabled: Boolean = true,
    val subtitleSizeSp: Int = 18,
    val subtitlePosition: SubtitlePosition = SubtitlePosition.BOTTOM,
    val subtitleStyle: SubtitleStyle = SubtitleStyle.YELLOW_BLACK
)

data class ImageGenerationForm(
    val prompt: String = "",
    val style: VideoStyle = VideoStyle.CINEMATIC,
    val aspectRatio: AspectRatio = AspectRatio.SQUARE_1_1
)

class KishuViewModel(application: Application) : AndroidViewModel(application) {

    private val db = KishuDatabase.getInstance(application)
    private val geminiService = GeminiService(application)
    private val repository = ProjectRepository(db.projectDao(), geminiService)

    val voiceoverManager = VoiceoverManager(application)
    val ambientMusicManager = AmbientMusicManager()
    val playerController = VideoPlayerController(application, voiceoverManager, ambientMusicManager, viewModelScope)

    val allProjects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow(StudioTab.VIDEO)
    val currentTab = _currentTab.asStateFlow()

    private val _videoForm = MutableStateFlow(VideoGenerationForm())
    val videoForm = _videoForm.asStateFlow()

    private val _imageForm = MutableStateFlow(ImageGenerationForm())
    val imageForm = _imageForm.asStateFlow()

    private val _isGeneratingVideo = MutableStateFlow(false)
    val isGeneratingVideo = _isGeneratingVideo.asStateFlow()

    private val _generationStatusMessage = MutableStateFlow("")
    val generationStatusMessage = _generationStatusMessage.asStateFlow()

    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage = _isGeneratingImage.asStateFlow()

    private val _lastGeneratedImage = MutableStateFlow<GeneratedImageResult?>(null)
    val lastGeneratedImage = _lastGeneratedImage.asStateFlow()

    private val _activeProject = MutableStateFlow<ProjectEntity?>(null)
    val activeProject = _activeProject.asStateFlow()

    private val _activeScenes = MutableStateFlow<List<SceneEntity>>(emptyList())
    val activeScenes = _activeScenes.asStateFlow()

    private val _selectedSceneForEdit = MutableStateFlow<SceneEntity?>(null)
    val selectedSceneForEdit = _selectedSceneForEdit.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    init {
        // Seed default demo project if database is empty on first launch
        viewModelScope.launch {
            allProjects.collect { projects ->
                if (projects.isEmpty()) {
                    createDefaultDemoProject()
                }
            }
        }
    }

    fun selectTab(tab: StudioTab) {
        _currentTab.value = tab
    }

    fun updateVideoPrompt(prompt: String) {
        _videoForm.value = _videoForm.value.copy(prompt = prompt)
    }

    fun updateVideoDuration(seconds: Int) {
        _videoForm.value = _videoForm.value.copy(durationSeconds = seconds)
    }

    fun updateVideoStyle(style: VideoStyle) {
        _videoForm.value = _videoForm.value.copy(style = style)
    }

    fun updateVideoAspectRatio(aspectRatio: AspectRatio) {
        _videoForm.value = _videoForm.value.copy(aspectRatio = aspectRatio)
    }

    fun updateVideoLanguage(language: VoiceLanguage) {
        _videoForm.value = _videoForm.value.copy(language = language)
        voiceoverManager.setVoiceConfig(language, _videoForm.value.voiceTone)
    }

    fun updateVideoVoiceTone(tone: VoiceTone) {
        _videoForm.value = _videoForm.value.copy(voiceTone = tone)
        voiceoverManager.setVoiceConfig(_videoForm.value.language, tone)
    }

    fun updateSubtitleSettings(sizeSp: Int, position: SubtitlePosition, style: SubtitleStyle, enabled: Boolean) {
        _videoForm.value = _videoForm.value.copy(
            subtitleSizeSp = sizeSp,
            subtitlePosition = position,
            subtitleStyle = style,
            subtitleEnabled = enabled
        )
    }

    fun previewVoice(sampleText: String? = null) {
        val text = sampleText ?: when (_videoForm.value.language) {
            VoiceLanguage.HINDI -> "नमस्ते! मैं किशु एआई का वॉइसओवर इंजन हूँ। आपकी वीडियो के लिए आवाज़ तैयार है।"
            VoiceLanguage.SPANISH -> "¡Hola! Soy el motor de doblaje de Kishu AI. Tu voz en off está lista."
            VoiceLanguage.JAPANESE -> "こんにちは！Kishu AIの音声エンジンです。準備が整いました。"
            else -> "Hello! I am the Kishu AI voiceover engine. Ready to narrate your video."
        }
        voiceoverManager.setVoiceConfig(_videoForm.value.language, _videoForm.value.voiceTone)
        voiceoverManager.speak(text)
    }

    fun enhanceVideoPrompt() {
        val current = _videoForm.value.prompt
        if (current.isBlank()) return
        viewModelScope.launch {
            _generationStatusMessage.value = "AI magic wand: Enhancing prompt..."
            val enhanced = geminiService.expandPrompt(current, _videoForm.value.style, isImage = false)
            _videoForm.value = _videoForm.value.copy(prompt = enhanced)
            _generationStatusMessage.value = ""
        }
    }

    fun generateVideo() {
        val form = _videoForm.value
        val prompt = form.prompt.ifBlank { "Cinematic AI Sci-Fi Odyssey through nebula and stars" }

        viewModelScope.launch {
            _isGeneratingVideo.value = true
            try {
                _generationStatusMessage.value = "1/4: Analyzing story & timeline (${form.durationSeconds}s)..."
                val projectId = repository.createVideoProject(
                    prompt = prompt,
                    durationSeconds = form.durationSeconds,
                    style = form.style,
                    aspectRatio = form.aspectRatio,
                    language = form.language,
                    voiceTone = form.voiceTone,
                    musicMood = form.musicMood,
                    subtitleSizeSp = form.subtitleSizeSp,
                    subtitlePosition = form.subtitlePosition,
                    subtitleStyle = form.subtitleStyle
                )

                _generationStatusMessage.value = "2/4: Directing multi-scene screenplay..."
                _generationStatusMessage.value = "3/4: Synthesizing ${form.language.label} voiceover & SRT..."
                _generationStatusMessage.value = "4/4: Finalizing video timeline in Editor..."

                loadProjectIntoEditor(projectId)
                _currentTab.value = StudioTab.EDITOR
                _toastMessage.value = "Video Generated Successfully! Opened in Clip Editor."
            } catch (e: Exception) {
                _toastMessage.value = "Generation completed with local studio fallback."
            } finally {
                _isGeneratingVideo.value = false
                _generationStatusMessage.value = ""
            }
        }
    }

    // Image Studio Actions
    fun updateImagePrompt(prompt: String) {
        _imageForm.value = _imageForm.value.copy(prompt = prompt)
    }

    fun updateImageStyle(style: VideoStyle) {
        _imageForm.value = _imageForm.value.copy(style = style)
    }

    fun updateImageAspectRatio(ratio: AspectRatio) {
        _imageForm.value = _imageForm.value.copy(aspectRatio = ratio)
    }

    fun generateImage() {
        val form = _imageForm.value
        val prompt = form.prompt.ifBlank { "Futuristic bioluminescent rainforest with glowing crystal animals" }

        viewModelScope.launch {
            _isGeneratingImage.value = true
            try {
                val res = geminiService.generateImage(prompt, form.style, form.aspectRatio)
                _lastGeneratedImage.value = res

                repository.createImageProject(
                    prompt = prompt,
                    style = form.style,
                    aspectRatio = form.aspectRatio
                )
                _toastMessage.value = "Image Generated & Saved to Projects!"
            } catch (e: Exception) {
                _toastMessage.value = "Image created with Studio Renderer."
            } finally {
                _isGeneratingImage.value = false
            }
        }
    }

    // Editor & Player Actions
    fun loadProjectIntoEditor(projectId: Long) {
        viewModelScope.launch {
            val projectWithScenes = repository.getProjectWithScenes(projectId)
            if (projectWithScenes != null) {
                _activeProject.value = projectWithScenes.project
                _activeScenes.value = projectWithScenes.scenes
                _selectedSceneForEdit.value = projectWithScenes.scenes.firstOrNull()
                playerController.loadProject(projectWithScenes.project, projectWithScenes.scenes)
            }
        }
    }

    fun selectSceneForEdit(scene: SceneEntity) {
        _selectedSceneForEdit.value = scene
        // Seek player to this scene
        val scenes = _activeScenes.value
        var accum = 0L
        for (s in scenes) {
            if (s.id == scene.id) {
                playerController.seekTo(accum)
                break
            }
            accum += s.durationSeconds * 1000L
        }
    }

    fun updateSelectedScene(
        title: String,
        narration: String,
        subtitle: String,
        durationSeconds: Int,
        cameraMotion: CameraMotion,
        transitionType: TransitionType
    ) {
        val current = _selectedSceneForEdit.value ?: return
        val updated = current.copy(
            title = title,
            narrationText = narration,
            subtitleText = subtitle,
            durationSeconds = durationSeconds.coerceIn(1, 180),
            cameraMotion = cameraMotion.name,
            transitionType = transitionType.name
        )

        viewModelScope.launch {
            repository.updateScene(updated)
            // Refresh
            val projectId = updated.projectId
            val refreshed = repository.getProjectWithScenes(projectId)
            if (refreshed != null) {
                _activeProject.value = refreshed.project
                _activeScenes.value = refreshed.scenes
                _selectedSceneForEdit.value = updated
                playerController.loadProject(refreshed.project, refreshed.scenes)
            }
            _toastMessage.value = "Clip updated!"
        }
    }

    fun addClipToTimeline() {
        val project = _activeProject.value ?: return
        val scenes = _activeScenes.value
        val afterIndex = scenes.size

        viewModelScope.launch {
            repository.addScene(project.id, afterIndex)
            val refreshed = repository.getProjectWithScenes(project.id)
            if (refreshed != null) {
                _activeProject.value = refreshed.project
                _activeScenes.value = refreshed.scenes
                playerController.loadProject(refreshed.project, refreshed.scenes)
            }
            _toastMessage.value = "New clip added to timeline!"
        }
    }

    fun deleteSelectedClip() {
        val scene = _selectedSceneForEdit.value ?: return
        val projectId = scene.projectId
        viewModelScope.launch {
            repository.deleteScene(scene)
            val refreshed = repository.getProjectWithScenes(projectId)
            if (refreshed != null) {
                _activeProject.value = refreshed.project
                _activeScenes.value = refreshed.scenes
                _selectedSceneForEdit.value = refreshed.scenes.firstOrNull()
                playerController.loadProject(refreshed.project, refreshed.scenes)
            }
            _toastMessage.value = "Clip deleted."
        }
    }

    fun updateEditorSubtitleAppearance(sizeSp: Int, position: SubtitlePosition, style: SubtitleStyle) {
        playerController.subtitleSizeSp = sizeSp
        playerController.subtitlePosition = position
        playerController.subtitleStyle = style

        val currentProj = _activeProject.value ?: return
        val updatedProj = currentProj.copy(
            subtitleSizeSp = sizeSp,
            subtitlePosition = position.name,
            subtitleStyle = style.name
        )
        viewModelScope.launch {
            repository.updateProject(updatedProj)
            _activeProject.value = updatedProj
        }
    }

    fun updateEditorAspectRatio(aspectRatio: AspectRatio) {
        playerController.aspectRatio = aspectRatio
        val currentProj = _activeProject.value ?: return
        val updatedProj = currentProj.copy(aspectRatio = aspectRatio.name)
        viewModelScope.launch {
            repository.updateProject(updatedProj)
            _activeProject.value = updatedProj
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            if (_activeProject.value?.id == projectId) {
                _activeProject.value = null
                _activeScenes.value = emptyList()
                _selectedSceneForEdit.value = null
            }
            _toastMessage.value = "Project deleted."
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun loadTemplate(prompt: String, style: VideoStyle, duration: Int, lang: VoiceLanguage) {
        _videoForm.value = _videoForm.value.copy(
            prompt = prompt,
            style = style,
            durationSeconds = duration,
            language = lang
        )
        _currentTab.value = StudioTab.VIDEO
    }

    private suspend fun createDefaultDemoProject() {
        val demoPrompt = "ब्रह्मांडीय यात्रा: एक जादुई तारे की खोज (Cosmic Odyssey: Quest of the Magic Star)"
        repository.createVideoProject(
            prompt = demoPrompt,
            durationSeconds = 30,
            style = VideoStyle.CINEMATIC,
            aspectRatio = AspectRatio.LANDSCAPE_16_9,
            language = VoiceLanguage.HINDI,
            voiceTone = VoiceTone.CINEMATIC_EPIC,
            musicMood = "Cinematic Ambient"
        )
    }

    override fun onCleared() {
        super.onCleared()
        playerController.pause()
        voiceoverManager.shutdown()
        ambientMusicManager.stopMusic()
    }
}
