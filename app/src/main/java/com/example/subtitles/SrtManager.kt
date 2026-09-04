package com.example.subtitles

import com.example.data.model.SceneEntity

data class SubtitleCue(
    val index: Int,
    val startMs: Long,
    val endMs: Long,
    val text: String
)

object SrtManager {

    fun formatTimestamp(ms: Long): String {
        val totalSeconds = ms / 1000
        val millis = ms % 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600
        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, millis)
    }

    fun parseTimestamp(timestamp: String): Long {
        return try {
            val parts = timestamp.trim().replace(".", ",").split(":")
            val hours = parts[0].toLong()
            val minutes = parts[1].toLong()
            val secMillis = parts[2].split(",")
            val seconds = secMillis[0].toLong()
            val millis = if (secMillis.size > 1) secMillis[1].padEnd(3, '0').take(3).toLong() else 0L
            (hours * 3600 + minutes * 60 + seconds) * 1000 + millis
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Generates standard SRT text from scenes list
     */
    fun generateSrtFromScenes(scenes: List<SceneEntity>): String {
        val sb = StringBuilder()
        var accumulatedMs = 0L

        scenes.forEachIndexed { index, scene ->
            val durationMs = scene.durationSeconds * 1000L
            val startMs = accumulatedMs
            val endMs = accumulatedMs + durationMs

            val text = if (scene.subtitleText.isNotBlank()) scene.subtitleText else scene.narrationText

            if (text.isNotBlank()) {
                sb.append(index + 1).append("\n")
                sb.append(formatTimestamp(startMs))
                    .append(" --> ")
                    .append(formatTimestamp(endMs))
                    .append("\n")
                sb.append(text).append("\n\n")
            }

            accumulatedMs += durationMs
        }

        return sb.toString().trim()
    }

    /**
     * Parses an SRT formatted string into structured cues
     */
    fun parseSrt(srtContent: String): List<SubtitleCue> {
        if (srtContent.isBlank()) return emptyList()

        val cues = mutableListOf<SubtitleCue>()
        val blocks = srtContent.replace("\r\n", "\n").replace("\r", "\n").split("\n\n")

        for (block in blocks) {
            val lines = block.trim().lines().filter { it.isNotBlank() }
            if (lines.size >= 2) {
                val timeLineIndex = if (lines[0].contains("-->")) 0 else 1
                if (lines.size > timeLineIndex) {
                    val timeLine = lines[timeLineIndex]
                    if (timeLine.contains("-->")) {
                        val times = timeLine.split("-->")
                        val startMs = parseTimestamp(times[0])
                        val endMs = parseTimestamp(times[1])
                        val text = lines.drop(timeLineIndex + 1).joinToString("\n")
                        val index = if (timeLineIndex > 0) lines[0].toIntOrNull() ?: (cues.size + 1) else (cues.size + 1)

                        cues.add(SubtitleCue(index, startMs, endMs, text))
                    }
                }
            }
        }
        return cues
    }

    /**
     * Finds the cue active at current millisecond
     */
    fun getActiveCue(cues: List<SubtitleCue>, currentMs: Long): SubtitleCue? {
        return cues.firstOrNull { currentMs in it.startMs..it.endMs }
    }
}
