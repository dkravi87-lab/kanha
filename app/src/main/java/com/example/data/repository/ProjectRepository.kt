package com.example.data.repository

import android.content.Context
import com.example.data.local.ProjectDao
import com.example.data.model.AspectRatio
import com.example.data.model.CameraMotion
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectType
import com.example.data.model.ProjectWithScenes
import com.example.data.model.SceneEntity
import com.example.data.model.SubtitlePosition
import com.example.data.model.SubtitleStyle
import com.example.data.model.TransitionType
import com.example.data.model.VideoStyle
import com.example.data.model.VoiceLanguage
import com.example.data.model.VoiceTone
import com.example.data.remote.GeminiService
import com.example.subtitles.SrtManager
import kotlinx.coroutines.flow.Flow

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val geminiService: GeminiService
) {

    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    suspend fun getProjectWithScenes(id: Long): ProjectWithScenes? {
        return projectDao.getProjectWithScenes(id)
    }

    suspend fun getProjectById(id: Long): ProjectEntity? {
        return projectDao.getProjectById(id)
    }

    suspend fun getScenes(projectId: Long): List<SceneEntity> {
        return projectDao.getScenesListForProject(projectId)
    }

    suspend fun createVideoProject(
        prompt: String,
        durationSeconds: Int,
        style: VideoStyle,
        aspectRatio: AspectRatio,
        language: VoiceLanguage,
        voiceTone: VoiceTone,
        musicMood: String,
        subtitleSizeSp: Int = 18,
        subtitlePosition: SubtitlePosition = SubtitlePosition.BOTTOM,
        subtitleStyle: SubtitleStyle = SubtitleStyle.YELLOW_BLACK
    ): Long {
        val storyboard = geminiService.generateStoryboard(
            userPrompt = prompt,
            durationSeconds = durationSeconds,
            style = style,
            aspectRatio = aspectRatio,
            language = language,
            voiceTone = voiceTone.label
        )

        val projectEntity = ProjectEntity(
            title = storyboard.title,
            type = ProjectType.VIDEO,
            prompt = prompt,
            durationSeconds = durationSeconds,
            style = style.name,
            aspectRatio = aspectRatio.name,
            language = language.name,
            voiceTone = voiceTone.name,
            subtitleEnabled = true,
            subtitleSizeSp = subtitleSizeSp,
            subtitlePosition = subtitlePosition.name,
            subtitleStyle = subtitleStyle.name,
            musicMood = musicMood,
            srtContent = storyboard.srtContent,
            createdAt = System.currentTimeMillis()
        )

        val projectId = projectDao.insertProject(projectEntity)

        val scenesWithProjectId = storyboard.scenes.map { scene ->
            scene.copy(projectId = projectId)
        }
        projectDao.insertScenes(scenesWithProjectId)

        return projectId
    }

    suspend fun createImageProject(
        prompt: String,
        style: VideoStyle,
        aspectRatio: AspectRatio
    ): Long {
        val result = geminiService.generateImage(prompt, style, aspectRatio)

        val projectEntity = ProjectEntity(
            title = prompt.take(30).trim(),
            type = ProjectType.IMAGE,
            prompt = result.enhancedPrompt,
            durationSeconds = 0,
            style = style.name,
            aspectRatio = aspectRatio.name,
            previewImageUrl = result.localFilePath ?: "",
            createdAt = System.currentTimeMillis()
        )

        return projectDao.insertProject(projectEntity)
    }

    suspend fun updateProject(project: ProjectEntity) {
        projectDao.updateProject(project)
    }

    suspend fun updateScene(scene: SceneEntity) {
        projectDao.updateScene(scene)
        // Refresh project's SRT
        val allScenes = projectDao.getScenesListForProject(scene.projectId)
        val newSrt = SrtManager.generateSrtFromScenes(allScenes)
        val project = projectDao.getProjectById(scene.projectId)
        if (project != null) {
            projectDao.updateProject(project.copy(srtContent = newSrt))
        }
    }

    suspend fun deleteScene(scene: SceneEntity) {
        projectDao.deleteSceneById(scene.id)
        val allScenes = projectDao.getScenesListForProject(scene.projectId)
        val newSrt = SrtManager.generateSrtFromScenes(allScenes)
        val project = projectDao.getProjectById(scene.projectId)
        if (project != null) {
            val newTotalDuration = allScenes.sumOf { it.durationSeconds }
            projectDao.updateProject(project.copy(srtContent = newSrt, durationSeconds = newTotalDuration))
        }
    }

    suspend fun addScene(projectId: Long, afterIndex: Int): SceneEntity {
        val allScenes = projectDao.getScenesListForProject(projectId)
        val newIndex = afterIndex + 1
        val newScene = SceneEntity(
            projectId = projectId,
            sceneIndex = newIndex,
            title = "New Scene $newIndex",
            visualPrompt = "Dynamic cinematic shot with vibrant lighting",
            narrationText = "यह नया दृश्य कहानी को आगे बढ़ाता है।",
            subtitleText = "यह नया दृश्य कहानी को आगे बढ़ाता है।",
            durationSeconds = 5,
            cameraMotion = CameraMotion.ZOOM_IN.name,
            transitionType = TransitionType.CROSSFADE.name
        )
        val id = projectDao.insertScene(newScene)
        val inserted = newScene.copy(id = id)

        // Re-index subsequent scenes
        val updatedScenes = projectDao.getScenesListForProject(projectId)
        val newSrt = SrtManager.generateSrtFromScenes(updatedScenes)
        val project = projectDao.getProjectById(projectId)
        if (project != null) {
            projectDao.updateProject(project.copy(
                srtContent = newSrt,
                durationSeconds = updatedScenes.sumOf { it.durationSeconds }
            ))
        }

        return inserted
    }

    suspend fun deleteProject(id: Long) {
        projectDao.deleteScenesForProject(id)
        projectDao.deleteProjectById(id)
    }

    suspend fun seedInitialDemoIfEmpty() {
        // Only seed if empty
        val list = projectDao.getScenesListForProject(1)
        // Check if database is empty by getting all projects
        // We'll let ViewModel trigger this on first launch
    }
}
