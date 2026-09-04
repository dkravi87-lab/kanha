package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import androidx.room.TypeConverters

enum class ProjectType {
    VIDEO,
    IMAGE
}

enum class VideoStyle(val displayName: String, val promptModifier: String) {
    CINEMATIC("Cinematic Movie", "photorealistic 8k, cinematic anamorphic lens, epic lighting, 35mm film still, masterpiece"),
    CARTOON_3D("3D Cartoon (Pixar Style)", "3D animated feature film style, cute expressive characters, vibrant lighting, Pixar Dreamworks quality"),
    ANIME_2D("Anime (Studio Ghibli / Makoto Shinkai)", "Japanese anime aesthetic, rich painted background, Makoto Shinkai lighting, vibrant emotional colors"),
    CLAYMATION("Claymation / Stop Motion", "Aardman style stop-motion clay animation, tactile clay textures, studio miniature lighting"),
    CYBERPUNK("Cyberpunk Sci-Fi", "neon drenched cyberpunk city, holographic reflections, futuristic rain slicked streets, dark synthwave"),
    PHOTOREALISTIC("Hyper-Realistic Drone", "8k hyper-realistic national geographic documentary footage, drone aerial view, crisp details"),
    FANTASY_MYTH("Mythological Fantasy", "ancient mythological fantasy, golden aura, divine temple, ethereal glowing atmosphere, majestic"),
    VINTAGE_RETRO("Vintage 70s/80s VHS", "vintage 1980s retro film look, subtle film grain, nostalgic color grading, warm analog glow")
}

enum class AspectRatio(val label: String, val ratioFloat: Float, val widthRatio: Int, val heightRatio: Int) {
    LANDSCAPE_16_9("16:9 (YouTube / TV)", 16f / 9f, 16, 9),
    PORTRAIT_9_16("9:16 (Reels / Shorts / TikTok)", 9f / 16f, 9, 16),
    SQUARE_1_1("1:1 (Instagram)", 1f, 1, 1),
    CLASSIC_4_3("4:3 (Standard)", 4f / 3f, 4, 3),
    CINEMA_21_9("21:9 (Cinematic Ultra-wide)", 21f / 9f, 21, 9)
}

enum class CameraMotion(val label: String) {
    ZOOM_IN("Zoom In (Dramatic)"),
    ZOOM_OUT("Zoom Out (Reveal)"),
    PAN_LEFT("Pan Left"),
    PAN_RIGHT("Pan Right"),
    STATIC("Static Shot")
}

enum class TransitionType(val label: String) {
    CROSSFADE("Crossfade"),
    SLIDE_LEFT("Slide Left"),
    ZOOM_TRANSITION("Zoom Transition"),
    CUT("Direct Cut")
}

enum class VoiceLanguage(val code: String, val label: String, val nativeName: String) {
    HINDI("hi_IN", "Hindi", "हिन्दी"),
    ENGLISH("en_US", "English", "English"),
    BENGALI("bn_IN", "Bengali", "বাংলা"),
    TAMIL("ta_IN", "Tamil", "தமிழ்"),
    TELUGU("te_IN", "Telugu", "తెలుగు"),
    SPANISH("es_ES", "Spanish", "Español"),
    FRENCH("fr_FR", "French", "Français"),
    GERMAN("de_DE", "German", "Deutsch"),
    JAPANESE("ja_JP", "Japanese", "日本語"),
    ARABIC("ar_SA", "Arabic", "العربية")
}

enum class VoiceTone(val label: String, val pitch: Float, val speed: Float) {
    DEEP_NARRATOR("Deep Narrator (गंभीर)", 0.85f, 0.95f),
    FRIENDLY("Friendly & Warm (दोस्ताना)", 1.05f, 1.0f),
    CINEMATIC_EPIC("Cinematic Dramatic (रोमांचक)", 0.8f, 0.9f),
    ENERGETIC("Energetic & Fast (उत्साही)", 1.15f, 1.15f),
    MEDITATIVE("Soft & Calm (शांत)", 0.95f, 0.85f)
}

enum class SubtitlePosition(val label: String) {
    BOTTOM("Bottom Screen"),
    CENTER("Center Screen"),
    TOP("Top Screen")
}

enum class SubtitleStyle(val label: String) {
    YELLOW_BLACK("Yellow on Dark (Classic Cinema)"),
    WHITE_OUTLINE("White with Heavy Outline"),
    NEON_CYAN("Neon Cyan Glow"),
    GLASS_BOX("Translucent Dark Box"),
    GOLDEN("Golden Luxury")
}

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: ProjectType = ProjectType.VIDEO,
    val prompt: String,
    val durationSeconds: Int = 30, // Can range from 15s to 1800s (30 minutes!)
    val style: String = VideoStyle.CINEMATIC.name,
    val aspectRatio: String = AspectRatio.LANDSCAPE_16_9.name,
    val language: String = VoiceLanguage.HINDI.name,
    val voiceTone: String = VoiceTone.DEEP_NARRATOR.name,
    val subtitleEnabled: Boolean = true,
    val subtitleSizeSp: Int = 18,
    val subtitlePosition: String = SubtitlePosition.BOTTOM.name,
    val subtitleStyle: String = SubtitleStyle.YELLOW_BLACK.name,
    val musicMood: String = "Cinematic Ambient",
    val srtContent: String = "",
    val previewImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "scenes")
data class SceneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val sceneIndex: Int,
    val title: String = "Scene",
    val visualPrompt: String,
    val narrationText: String,
    val subtitleText: String,
    val durationSeconds: Int = 5,
    val cameraMotion: String = CameraMotion.ZOOM_IN.name,
    val transitionType: String = TransitionType.CROSSFADE.name,
    val imageUrl: String = "",
    val accentColorHex: String = "#3B82F6"
)

data class ProjectWithScenes(
    @Embedded val project: ProjectEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "projectId"
    )
    val scenes: List<SceneEntity>
)

class KishuTypeConverters {
    @TypeConverter
    fun fromProjectType(type: ProjectType): String = type.name

    @TypeConverter
    fun toProjectType(value: String): ProjectType = try {
        ProjectType.valueOf(value)
    } catch (e: Exception) {
        ProjectType.VIDEO
    }
}
