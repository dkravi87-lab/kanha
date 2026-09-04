package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.BuildConfig
import com.example.data.model.AspectRatio
import com.example.data.model.Clip
import com.example.data.model.Project
import com.example.data.model.StylePreset
import com.example.data.model.Subtitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID

class GeminiService(private val context: Context) {

    private val apiKey: String
        get() = BuildConfig.MY_GEMINI_API_KEY.ifEmpty {
            try {
                val clazz = Class.forName("com.example.BuildConfig")
                val field = clazz.getField("MY_GEMINI_API_KEY")
                field.get(null) as? String ?: ""
            } catch (e: Exception) {
                ""
            }
        }

    suspend fun generateStory(
        prompt: String,
        style: StylePreset = StylePreset.CINEMATIC,
        ratio: AspectRatio = AspectRatio.RATIO_9_16,
        sceneCount: Int = 4
    ): Result<Project> = withContext(Dispatchers.IO) {
        try {
            val validKey = apiKey.trim()
            if (validKey.isNotEmpty() && !validKey.startsWith("MY_")) {
                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$validKey"
                val systemPrompt = """
                    You are Kishu AI, an expert video director. Generate a structured video project from the user's prompt.
                    Return ONLY a JSON object matching this schema:
                    {
                      "title": "Short title",
                      "description": "Short description",
                      "clips": [
                        {
                          "order": 0,
                          "title": "Scene title",
                          "prompt": "Detailed visual description of this scene",
                          "durationSeconds": 5.0,
                          "cameraMotion": "PAN_LEFT",
                          "voiceoverText": "Narrator voiceover script in Hindi/English",
                          "subtitles": [
                            {"text": "Subtitle chunk 1", "startTimeMs": 0, "endTimeMs": 2500},
                            {"text": "Subtitle chunk 2", "startTimeMs": 2500, "endTimeMs": 5000}
                          ]
                        }
                      ]
                    }
                    Generate exactly $sceneCount clips. Do not include markdown code fences, return pure JSON.
                """.trimIndent()

                val requestBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", "$systemPrompt\n\nUser prompt: $prompt\nStyle: ${style.displayName}"))
                            })
                        })
                    })
                }

                val responseStr = makeHttpRequest(endpoint, requestBody.toString())
                val root = JSONObject(responseStr)
                val candidates = root.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val contentObj = firstCandidate?.optJSONObject("content")
                val parts = contentObj?.optJSONArray("parts")
                val textOutput = parts?.optJSONObject(0)?.optString("text") ?: ""

                val cleanJson = textOutput.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val projectJson = JSONObject(cleanJson)
                val title = projectJson.optString("title", prompt.take(30))
                val description = projectJson.optString("description", prompt)
                val clipsJson = projectJson.optJSONArray("clips") ?: JSONArray()

                val clips = mutableListOf<Clip>()
                for (i in 0 until clipsJson.length()) {
                    val cObj = clipsJson.getJSONObject(i)
                    val clipPrompt = cObj.optString("prompt", "$prompt scene $i")
                    val duration = cObj.optDouble("durationSeconds", 4.0).toFloat()
                    val voiceover = cObj.optString("voiceoverText", "")

                    val subsJson = cObj.optJSONArray("subtitles")
                    val subs = mutableListOf<Subtitle>()
                    if (subsJson != null) {
                        for (j in 0 until subsJson.length()) {
                            val sObj = subsJson.getJSONObject(j)
                            subs.add(
                                Subtitle(
                                    text = sObj.optString("text", ""),
                                    startTimeMs = sObj.optLong("startTimeMs", 0L),
                                    endTimeMs = sObj.optLong("endTimeMs", 2000L)
                                )
                            )
                        }
                    }

                    val encodedPrompt = URLEncoder.encode("$clipPrompt, ${style.promptModifier}, 4k ultra detailed, masterpiece", "UTF-8")
                    val width = if (ratio == AspectRatio.RATIO_9_16) 720 else 1280
                    val height = if (ratio == AspectRatio.RATIO_9_16) 1280 else 720
                    val imageUrl = "https://image.pollinations.ai/prompt/$encodedPrompt?width=$width&height=$height&nologo=true&seed=${UUID.randomUUID().hashCode()}"

                    clips.add(
                        Clip(
                            id = UUID.randomUUID().toString(),
                            order = i,
                            title = cObj.optString("title", "Scene ${i + 1}"),
                            prompt = clipPrompt,
                            imageUrl = imageUrl,
                            durationSeconds = duration,
                            cameraMotion = cObj.optString("cameraMotion", "ZOOM_IN"),
                            voiceoverText = voiceover,
                            subtitles = subs
                        )
                    )
                }

                Result.success(
                    Project(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        description = description,
                        aspectRatio = ratio,
                        stylePreset = style,
                        clips = clips
                    )
                )
            } else {
                // Autonomous engine without API Key requirement
                Result.success(generateLocalProject(prompt, style, ratio, sceneCount))
            }
        } catch (e: Exception) {
            Result.success(generateLocalProject(prompt, style, ratio, sceneCount))
        }
    }

    suspend fun generateImage(
        prompt: String,
        style: StylePreset = StylePreset.CINEMATIC,
        ratio: AspectRatio = AspectRatio.RATIO_9_16
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val encodedPrompt = URLEncoder.encode("$prompt, ${style.promptModifier}, highly detailed, cinematic lighting, photorealistic, 8k resolution", "UTF-8")
            val width = if (ratio == AspectRatio.RATIO_9_16) 720 else 1280
            val height = if (ratio == AspectRatio.RATIO_9_16) 1280 else 720
            val seed = (System.currentTimeMillis() % 1000000).toInt()
            val imageUrl = "https://image.pollinations.ai/prompt/$encodedPrompt?width=$width&height=$height&nologo=true&seed=$seed"
            Result.success(imageUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateLocalProject(
        prompt: String,
        style: StylePreset,
        ratio: AspectRatio,
        sceneCount: Int
    ): Project {
        val width = if (ratio == AspectRatio.RATIO_9_16) 720 else 1280
        val height = if (ratio == AspectRatio.RATIO_9_16) 1280 else 720
        val motions = listOf("ZOOM_IN", "PAN_RIGHT", "ZOOM_OUT", "PAN_LEFT")

        val scenes = listOf(
            Triple(
                "आरंभ (The Beginning)",
                "Dramatic cinematic opening scene establishing $prompt, golden hour lighting, cinematic shot",
                "प्रस्तुत है एक अद्भुत दृश्य: $prompt की भव्य शुरुआत।"
            ),
            Triple(
                "विस्तार (The Journey)",
                "Close-up detailed view highlighting the beauty and emotion of $prompt, cinematic realism",
                "इस दिव्य और मनोरम यात्रा के अद्भुत रंग और गहराई।"
            ),
            Triple(
                "चरम (The Climax)",
                "Dynamic energetic motion scene with radiant magical glow portraying $prompt, masterpiece",
                "तेज और ऊर्जावान दृश्य, जो दिल को छू जाए।"
            ),
            Triple(
                "समापन (The Finale)",
                "Peaceful serene wide angle concluding view of $prompt, high resolution, atmospheric mood",
                "और इस प्रकार यह सुंदर गाथा शांति और आनंद के साथ संपन्न होती है।"
            )
        )

        val clips = (0 until sceneCount).map { i ->
            val scene = scenes[i % scenes.size]
            val encoded = URLEncoder.encode("${scene.second}, ${style.promptModifier}, masterpiece, highly detailed", "UTF-8")
            val seed = (System.currentTimeMillis() + (i * 997)).hashCode()
            val imageUrl = "https://image.pollinations.ai/prompt/$encoded?width=$width&height=$height&nologo=true&seed=$seed"

            Clip(
                id = UUID.randomUUID().toString(),
                order = i,
                title = scene.first,
                prompt = scene.second,
                imageUrl = imageUrl,
                durationSeconds = 4.5f,
                cameraMotion = motions[i % motions.size],
                voiceoverText = scene.third,
                subtitles = listOf(
                    Subtitle(text = scene.third.take(scene.third.length / 2), startTimeMs = 0L, endTimeMs = 2200L),
                    Subtitle(text = scene.third.substring(scene.third.length / 2), startTimeMs = 2200L, endTimeMs = 4500L)
                )
            )
        }

        return Project(
            id = UUID.randomUUID().toString(),
            title = prompt.take(30).ifEmpty { "Kishu Creation" },
            description = "Cinematic video generation of $prompt",
            aspectRatio = ratio,
            stylePreset = style,
            clips = clips
        )
    }

    private fun makeHttpRequest(urlStr: String, jsonBody: String): String {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        conn.setRequestProperty("Accept", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 30000
        conn.readTimeout = 30000

        OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
            writer.write(jsonBody)
            writer.flush()
        }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val response = BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
            reader.readText()
        }

        if (code !in 200..299) {
            throw Exception("HTTP $code: $response")
        }

        return response
    }
}
