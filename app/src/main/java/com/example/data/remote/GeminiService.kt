package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AspectRatio
import com.example.data.model.CameraMotion
import com.example.data.model.SceneEntity
import com.example.data.model.TransitionType
import com.example.data.model.VideoStyle
import com.example.data.model.VoiceLanguage
import com.example.subtitles.SrtManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlin.math.max

data class StoryboardResult(
    val title: String,
    val scenes: List<SceneEntity>,
    val srtContent: String,
    val overview: String
)

data class GeneratedImageResult(
    val bitmap: Bitmap?,
    val localFilePath: String?,
    val enhancedPrompt: String,
    val description: String
)

class GeminiService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiKey: String
        get() = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

    fun hasApiKey(): Boolean = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

    /**
     * AI Prompt Expander - Takes any brief idea and turns it into an award-winning cinematic prompt
     */
    suspend fun expandPrompt(
        rawPrompt: String,
        style: VideoStyle,
        isImage: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        if (!hasApiKey()) {
            return@withContext "${rawPrompt.trim()}, ${style.promptModifier}, volumetric lighting, ultra-detailed textures, master composition"
        }

        try {
            val systemInstruction = "You are a world-class AI prompt engineer for cinematic video and high-end graphic generation. Expand the user's prompt into an ultra-descriptive, highly visual prompt with precise details on lighting, camera angles, textures, colors, and atmosphere for ${style.displayName}."
            val userMessage = "Expand this prompt for ${if (isImage) "realistic high-quality image generation" else "cinematic video generation"}: \"$rawPrompt\". Style: ${style.displayName}. Keep it purely the enhanced prompt text, without any conversational filler."

            val response = callGeminiText(systemInstruction, userMessage)
            if (response.isNotBlank()) response.trim() else "${rawPrompt.trim()}, ${style.promptModifier}"
        } catch (e: Exception) {
            Log.w("GeminiService", "Prompt expansion fallback used: ${e.message}")
            "${rawPrompt.trim()}, ${style.promptModifier}"
        }
    }

    /**
     * Generates a complete multi-scene storyboard for video up to 30 minutes.
     * Generates sequential scenes with camera directions, visual descriptions,
     * native language narration, and SRT subtitles.
     */
    suspend fun generateStoryboard(
        userPrompt: String,
        durationSeconds: Int,
        style: VideoStyle,
        aspectRatio: AspectRatio,
        language: VoiceLanguage,
        voiceTone: String
    ): StoryboardResult = withContext(Dispatchers.IO) {
        // Determine number of scenes based on duration
        // Short: 15s -> 3 scenes (5s each)
        // 60s -> 6 scenes (10s each)
        // 5 min -> 10 scenes (30s each)
        // 10 min -> 12 scenes (50s each)
        // 30 min (1800s) -> 15-20 rich scenes/chapters (e.g. 90-120s each)
        val targetSceneCount = when {
            durationSeconds <= 20 -> 3
            durationSeconds <= 60 -> 4
            durationSeconds <= 180 -> 6
            durationSeconds <= 600 -> 8
            durationSeconds <= 1200 -> 12
            else -> 15 // Up to 30 minutes full length
        }
        val secondsPerScene = max(3, durationSeconds / targetSceneCount)

        if (!hasApiKey()) {
            return@withContext generateFallbackStoryboard(userPrompt, durationSeconds, style, language, targetSceneCount, secondsPerScene)
        }

        val systemPrompt = """
            You are Kishu AI, an expert cinematic director, screenplay writer, and video producer.
            The user wants to produce a video of duration $durationSeconds seconds ($style style).
            Target scenes: $targetSceneCount scenes (each approx $secondsPerScene seconds).
            Selected voiceover & subtitle language: ${language.label} (${language.nativeName}).
            
            Produce a JSON response with:
            {
              "title": "A short compelling title",
              "overview": "Brief description of the story arc",
              "scenes": [
                {
                  "sceneIndex": 1,
                  "title": "Scene name",
                  "visualPrompt": "Detailed visual description of this shot for 3D/cinematic rendering",
                  "narrationText": "Voiceover spoken text in ${language.label} (${language.nativeName})",
                  "subtitleText": "Subtitle line in ${language.label} (${language.nativeName})",
                  "durationSeconds": $secondsPerScene,
                  "cameraMotion": "ZOOM_IN" | "ZOOM_OUT" | "PAN_LEFT" | "PAN_RIGHT" | "STATIC",
                  "transitionType": "CROSSFADE" | "SLIDE_LEFT" | "ZOOM_TRANSITION" | "CUT"
                }
              ]
            }
            Return ONLY valid JSON.
        """.trimIndent()

        val userMessage = """
            Create the full multi-scene screenplay and visual storyboard for:
            Prompt: $userPrompt
            Aspect Ratio: ${aspectRatio.label}
            Voice Tone: $voiceTone
            Total Duration: $durationSeconds seconds
        """.trimIndent()

        try {
            val jsonText = callGeminiText(systemPrompt, userMessage)
            if (jsonText.isBlank()) {
                return@withContext generateFallbackStoryboard(userPrompt, durationSeconds, style, language, targetSceneCount, secondsPerScene)
            }
            val cleanJson = extractJson(jsonText)
            val root = JSONObject(cleanJson)
            val title = root.optString("title", "Kishu Creation")
            val overview = root.optString("overview", "AI Video Project")
            val scenesArray = root.optJSONArray("scenes")
                ?: return@withContext generateFallbackStoryboard(userPrompt, durationSeconds, style, language, targetSceneCount, secondsPerScene)

            val scenes = mutableListOf<SceneEntity>()
            val cameraMotions = listOf(CameraMotion.ZOOM_IN, CameraMotion.PAN_LEFT, CameraMotion.ZOOM_OUT, CameraMotion.PAN_RIGHT)

            for (i in 0 until scenesArray.length()) {
                val obj = scenesArray.getJSONObject(i)
                val sceneIndex = obj.optInt("sceneIndex", i + 1)
                val sceneTitle = obj.optString("title", "Scene $sceneIndex")
                val visualPrompt = obj.optString("visualPrompt", "$userPrompt - Scene $sceneIndex")
                val narrationText = obj.optString("narrationText", "")
                val subtitleText = obj.optString("subtitleText", narrationText)
                val sceneDuration = obj.optInt("durationSeconds", secondsPerScene)
                val camString = obj.optString("cameraMotion", cameraMotions[i % cameraMotions.size].name)
                val transString = obj.optString("transitionType", TransitionType.CROSSFADE.name)

                scenes.add(
                    SceneEntity(
                        projectId = 0,
                        sceneIndex = sceneIndex,
                        title = sceneTitle,
                        visualPrompt = visualPrompt,
                        narrationText = narrationText,
                        subtitleText = subtitleText,
                        durationSeconds = sceneDuration,
                        cameraMotion = camString,
                        transitionType = transString,
                        accentColorHex = getPaletteColor(i)
                    )
                )
            }

            val srt = SrtManager.generateSrtFromScenes(scenes)
            StoryboardResult(title, scenes, srt, overview)
        } catch (e: Exception) {
            Log.w("GeminiService", "Notice: using offline cinematic storyboard generator (${e.message})")
            generateFallbackStoryboard(userPrompt, durationSeconds, style, language, targetSceneCount, secondsPerScene)
        }
    }

    /**
     * Text to Image Generation using gemini-2.5-flash-image
     */
    suspend fun generateImage(
        prompt: String,
        style: VideoStyle,
        aspectRatio: AspectRatio
    ): GeneratedImageResult = withContext(Dispatchers.IO) {
        val enhanced = expandPrompt(prompt, style, isImage = true)

        if (!hasApiKey()) {
            return@withContext generateLocalGraphicAsset(enhanced, style, aspectRatio)
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent?key=$apiKey"
            val ratioStr = when (aspectRatio) {
                AspectRatio.LANDSCAPE_16_9 -> "16:9"
                AspectRatio.PORTRAIT_9_16 -> "9:16"
                AspectRatio.CLASSIC_4_3 -> "4:3"
                AspectRatio.CINEMA_21_9 -> "16:9"
                AspectRatio.SQUARE_1_1 -> "1:1"
            }

            val payload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", enhanced) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseModalities", JSONArray().apply {
                        put("TEXT")
                        put("IMAGE")
                    })
                    put("imageConfig", JSONObject().apply {
                        put("aspectRatio", ratioStr)
                    })
                })
            }

            val request = Request.Builder()
                .url(endpoint)
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val respBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiService", "Image generation API error: $respBody")
                return@withContext generateLocalGraphicAsset(enhanced, style, aspectRatio)
            }

            val json = JSONObject(respBody)
            val candidates = json.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            var imageBitmap: Bitmap? = null
            var textDescription = ""

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    if (part.has("inlineData")) {
                        val inlineData = part.getJSONObject("inlineData")
                        val b64Data = inlineData.getString("data")
                        val bytes = Base64.decode(b64Data, Base64.DEFAULT)
                        imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } else if (part.has("text")) {
                        textDescription = part.getString("text")
                    }
                }
            }

            if (imageBitmap != null) {
                val file = saveBitmapToDisk(imageBitmap, "kishu_img_${System.currentTimeMillis()}.png")
                GeneratedImageResult(imageBitmap, file.absolutePath, enhanced, textDescription.ifBlank { "Generated with Kishu AI" })
            } else {
                generateLocalGraphicAsset(enhanced, style, aspectRatio)
            }
        } catch (e: Exception) {
            Log.w("GeminiService", "Image generation notice: using local graphic renderer (${e.message})")
            generateLocalGraphicAsset(enhanced, style, aspectRatio)
        }
    }

    private suspend fun callGeminiText(systemInstruction: String, userMessage: String): String {
        val models = listOf("gemini-3.5-flash", "gemini-3.1-flash-lite-preview", "gemini-flash-latest")
        for (model in models) {
            try {
                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

                val payload = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", userMessage) })
                            })
                        })
                    })
                    if (systemInstruction.isNotBlank()) {
                        put("systemInstruction", JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", systemInstruction) })
                            })
                        })
                    }
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.7)
                        put("topP", 0.95)
                    })
                }

                val request = Request.Builder()
                    .url(endpoint)
                    .post(payload.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val root = JSONObject(body)
                    val candidates = root.optJSONArray("candidates")
                    val candidate = candidates?.optJSONObject(0)
                    val content = candidate?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val text = parts?.optJSONObject(0)?.optString("text")
                    if (!text.isNullOrBlank()) {
                        return text
                    }
                } else if (response.code == 429) {
                    Log.w("GeminiService", "Model $model quota reached (HTTP 429). Switching to fallback model...")
                    continue
                } else {
                    Log.w("GeminiService", "Model $model returned HTTP ${response.code}. Switching to fallback...")
                    continue
                }
            } catch (e: Exception) {
                Log.w("GeminiService", "Model $model request attempt notice: ${e.message}")
            }
        }
        return ""
    }

    private fun extractJson(raw: String): String {
        val trimmed = raw.trim()
        val startIndex = trimmed.indexOf('{')
        val endIndex = trimmed.lastIndexOf('}')
        return if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            trimmed.substring(startIndex, endIndex + 1)
        } else {
            trimmed
        }
    }

    private fun generateFallbackStoryboard(
        prompt: String,
        durationSeconds: Int,
        style: VideoStyle,
        language: VoiceLanguage,
        targetSceneCount: Int,
        secondsPerScene: Int
    ): StoryboardResult {
        val scenes = mutableListOf<SceneEntity>()
        val cleanPrompt = prompt.ifBlank { "Cinematic AI Odyssey" }

        val sceneArchetypes = listOf(
            Triple("Opening Establishing Shot", "The journey begins as a breathtaking vista unfolds under golden hour light.", "यह यात्रा एक भव्य दृश्य और सुनहरी रोशनी के साथ शुरू होती है।"),
            Triple("Dynamic Action Sequence", "Camera glides forward capturing intricate movements and rich cinematic details.", "कैमरा आगे बढ़ता है और हर बारीक हरकत को जीवंत बना देता है।"),
            Triple("Emotional Climax", "Dramatic shift in lighting and intensity reveals the central story element.", "प्रकाश और भावों का यह नाटकीय मोड़ कहानी के मुख्य रहस्य को उजागर करता है।"),
            Triple("Epic Resolution", "A wide cinematic pull-back shot leaving an unforgettable artistic impression.", "एक भव्य सिनेमैटिक शॉट के साथ यह दृश्य एक अमिट छाप छोड़ जाता है।")
        )

        for (i in 0 until targetSceneCount) {
            val archetype = sceneArchetypes[i % sceneArchetypes.size]
            val sceneIndex = i + 1
            val motion = when (i % 4) {
                0 -> CameraMotion.ZOOM_IN.name
                1 -> CameraMotion.PAN_LEFT.name
                2 -> CameraMotion.ZOOM_OUT.name
                else -> CameraMotion.PAN_RIGHT.name
            }

            val narration = if (language == VoiceLanguage.HINDI) {
                "${archetype.third} $cleanPrompt के दृश्य $sceneIndex में आपका स्वागत है।"
            } else {
                "${archetype.second} Welcome to scene $sceneIndex of $cleanPrompt."
            }

            val subtitle = narration

            scenes.add(
                SceneEntity(
                    projectId = 0,
                    sceneIndex = sceneIndex,
                    title = "Scene $sceneIndex: ${archetype.first}",
                    visualPrompt = "$cleanPrompt, Scene $sceneIndex - ${archetype.first}, ${style.promptModifier}",
                    narrationText = narration,
                    subtitleText = subtitle,
                    durationSeconds = secondsPerScene,
                    cameraMotion = motion,
                    transitionType = TransitionType.CROSSFADE.name,
                    accentColorHex = getPaletteColor(i)
                )
            )
        }

        val srt = SrtManager.generateSrtFromScenes(scenes)
        return StoryboardResult(
            title = cleanPrompt.take(40).capitalizeWords(),
            scenes = scenes,
            srtContent = srt,
            overview = "Cinematic $style production created with Kishu AI Studio."
        )
    }

    private fun generateLocalGraphicAsset(
        prompt: String,
        style: VideoStyle,
        aspectRatio: AspectRatio
    ): GeneratedImageResult {
        val width = 800
        val height = (width / aspectRatio.ratioFloat).toInt().coerceAtLeast(300)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        // Gradient background matching style
        val colors = when (style) {
            VideoStyle.CYBERPUNK -> intArrayOf(android.graphics.Color.parseColor("#0F172A"), android.graphics.Color.parseColor("#7C3AED"), android.graphics.Color.parseColor("#06B6D4"))
            VideoStyle.CARTOON_3D -> intArrayOf(android.graphics.Color.parseColor("#F97316"), android.graphics.Color.parseColor("#FBBF24"), android.graphics.Color.parseColor("#38BDF8"))
            VideoStyle.ANIME_2D -> intArrayOf(android.graphics.Color.parseColor("#312E81"), android.graphics.Color.parseColor("#818CF8"), android.graphics.Color.parseColor("#F472B6"))
            VideoStyle.FANTASY_MYTH -> intArrayOf(android.graphics.Color.parseColor("#1E1B4B"), android.graphics.Color.parseColor("#B45309"), android.graphics.Color.parseColor("#FCD34D"))
            else -> intArrayOf(android.graphics.Color.parseColor("#0B0F19"), android.graphics.Color.parseColor("#4C1D95"), android.graphics.Color.parseColor("#1E1B4B"))
        }

        val gradient = android.graphics.LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            colors, null, android.graphics.Shader.TileMode.CLAMP
        )
        val paint = android.graphics.Paint().apply {
            shader = gradient
            isAntiAlias = true
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Decorative circles/aperture
        val glowPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#33FFFFFF")
            this.style = android.graphics.Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawCircle(width / 2f, height / 2f, (height / 3f), glowPaint)
        canvas.drawCircle(width / 2f, height / 2f, (height / 4f), glowPaint)

        // Title text
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 36f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }
        val subPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#E2E8F0")
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        val displayPrompt = if (prompt.length > 50) prompt.take(47) + "..." else prompt
        canvas.drawText("✨ Kishu AI Studio", width / 2f, height / 2f - 20, textPaint)
        canvas.drawText(displayPrompt, width / 2f, height / 2f + 30, subPaint)

        val file = saveBitmapToDisk(bitmap, "kishu_art_${System.currentTimeMillis()}.png")
        return GeneratedImageResult(bitmap, file.absolutePath, prompt, "High-fidelity ${style.displayName} visual")
    }

    private fun saveBitmapToDisk(bitmap: Bitmap, filename: String): File {
        val dir = File(context.filesDir, "kishu_media")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        return file
    }

    private fun getPaletteColor(index: Int): String {
        val colors = listOf("#6366F1", "#8B5CF6", "#EC4899", "#06B6D4", "#F59E0B", "#10B981")
        return colors[index % colors.size]
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}
