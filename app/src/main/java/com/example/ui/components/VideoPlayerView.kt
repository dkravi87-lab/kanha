package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AspectRatio
import com.example.data.model.CameraMotion
import com.example.data.model.SceneEntity
import com.example.data.model.SubtitlePosition
import com.example.data.model.SubtitleStyle
import com.example.player.VideoPlayerController
import com.example.ui.theme.KishuCardBorder
import com.example.ui.theme.KishuDarkSurface
import com.example.ui.theme.KishuPrimary
import com.example.ui.theme.KishuSecondary
import com.example.ui.theme.KishuSubtitleYellow

@Composable
fun VideoPlayerView(
    controller: VideoPlayerController,
    modifier: Modifier = Modifier
) {
    var showControls by remember { mutableStateOf(true) }
    val currentScene = controller.getCurrentScene()
    val progress = controller.sceneProgress

    // Calculate camera motion transformation (Ken Burns effect)
    val (scale, offsetX, offsetY) = remember(currentScene?.cameraMotion, progress) {
        when (currentScene?.cameraMotion) {
            CameraMotion.ZOOM_IN.name -> Triple(1.0f + progress * 0.25f, 0f, 0f)
            CameraMotion.ZOOM_OUT.name -> Triple(1.25f - progress * 0.25f, 0f, 0f)
            CameraMotion.PAN_LEFT.name -> Triple(1.15f, progress * 40f - 20f, 0f)
            CameraMotion.PAN_RIGHT.name -> Triple(1.15f, 20f - progress * 40f, 0f)
            else -> Triple(1.0f, 0f, 0f)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .border(1.dp, KishuCardBorder, RoundedCornerShape(16.dp))
            .testTag("video_player_container")
    ) {
        // Video Stage Box with Selected Aspect Ratio
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(controller.aspectRatio.ratioFloat)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(Color(0xFF070913))
                .clickable { showControls = !showControls }
        ) {
            // Scene Visual Stage with Camera Motion
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
            ) {
                if (!currentScene?.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = currentScene?.imageUrl,
                        contentDescription = "Scene visual frame",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Procedural Dynamic Scene Background Art
                    DynamicSceneGraphic(scene = currentScene)
                }
            }

            // Top Info Bar: Aspect Ratio, Scene Index & Duration
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, KishuCardBorder)
                ) {
                    Text(
                        text = "Clip ${controller.currentSceneIndex + 1} • ${controller.aspectRatio.label.take(4)}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = KishuPrimary.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = "Kishu Studio 4K",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Subtitles Overlay (Customizable Size, Position, Style)
            if (controller.subtitleEnabled && controller.activeSubtitleText.isNotBlank()) {
                val subtitleAlignment = when (controller.subtitlePosition) {
                    SubtitlePosition.TOP -> Alignment.TopCenter
                    SubtitlePosition.CENTER -> Alignment.Center
                    SubtitlePosition.BOTTOM -> Alignment.BottomCenter
                }

                val topPadding = if (controller.subtitlePosition == SubtitlePosition.TOP) 44.dp else 12.dp
                val bottomPadding = if (controller.subtitlePosition == SubtitlePosition.BOTTOM) 24.dp else 12.dp

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(subtitleAlignment)
                        .padding(start = 16.dp, end = 16.dp, top = topPadding, bottom = bottomPadding),
                    contentAlignment = Alignment.Center
                ) {
                    SubtitleRenderBox(
                        text = controller.activeSubtitleText,
                        sizeSp = controller.subtitleSizeSp,
                        style = controller.subtitleStyle
                    )
                }
            }

            // Playback state quick indicator overlay
            if (!controller.isPlaying && showControls) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .border(1.dp, KishuPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play preview",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Timeline Scrub Bar & Playback Controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KishuDarkSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Time Sliders & Duration Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDuration(controller.currentMs),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatDuration(controller.totalDurationMs),
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                Slider(
                    value = controller.currentMs.toFloat(),
                    onValueChange = { controller.seekTo(it.toLong()) },
                    valueRange = 0f..controller.totalDurationMs.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = KishuSecondary,
                        activeTrackColor = KishuPrimary,
                        inactiveTrackColor = Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .testTag("timeline_slider")
                )

                // Media Playback Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Prev Scene, Rewind 5s
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { controller.prevScene() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.SkipPrevious, "Previous Scene", tint = Color.White)
                        }
                        IconButton(
                            onClick = { controller.seekTo(controller.currentMs - 5000L) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.FastRewind, "Rewind 5s", tint = Color.White)
                        }
                    }

                    // Center: Big Play/Pause
                    IconButton(
                        onClick = { controller.togglePlayPause() },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Brush.linearGradient(listOf(KishuPrimary, KishuSecondary)),
                                CircleShape
                            )
                            .testTag("play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (controller.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (controller.isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Right: Forward 5s, Next Scene, Mute, Speed
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { controller.seekTo(controller.currentMs + 5000L) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.FastForward, "Forward 5s", tint = Color.White)
                        }
                        IconButton(
                            onClick = { controller.nextScene() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.SkipNext, "Next Scene", tint = Color.White)
                        }
                        IconButton(
                            onClick = { controller.toggleMute() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (controller.isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Audio toggle",
                                tint = if (controller.isMuted) Color.Red else Color.White
                            )
                        }
                        // Speed Chip
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier
                                .clickable {
                                    val nextSpeed = when (controller.playbackSpeed) {
                                        1.0f -> 1.5f
                                        1.5f -> 2.0f
                                        2.0f -> 0.5f
                                        else -> 1.0f
                                    }
                                    controller.setSpeed(nextSpeed)
                                }
                                .padding(start = 4.dp)
                        ) {
                            Text(
                                text = "${controller.playbackSpeed}x",
                                color = KishuSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubtitleRenderBox(
    text: String,
    sizeSp: Int,
    style: SubtitleStyle,
    modifier: Modifier = Modifier
) {
    when (style) {
        SubtitleStyle.YELLOW_BLACK -> {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.Black.copy(alpha = 0.75f),
                modifier = modifier
            ) {
                Text(
                    text = text,
                    color = KishuSubtitleYellow,
                    fontSize = sizeSp.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        shadow = Shadow(color = Color.Black, offset = Offset(2f, 2f), blurRadius = 4f)
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        SubtitleStyle.WHITE_OUTLINE -> {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.Black.copy(alpha = 0.5f),
                modifier = modifier
            ) {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = sizeSp.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        shadow = Shadow(color = Color.Black, offset = Offset(3f, 3f), blurRadius = 6f)
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        SubtitleStyle.NEON_CYAN -> {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF081C2E).copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, KishuSecondary),
                modifier = modifier
            ) {
                Text(
                    text = text,
                    color = Color(0xFF67E8F9),
                    fontSize = sizeSp.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }
        SubtitleStyle.GLASS_BOX -> {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                modifier = modifier
            ) {
                Text(
                    text = text,
                    color = Color(0xFFF8FAFC),
                    fontSize = sizeSp.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        SubtitleStyle.GOLDEN -> {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF1A120B).copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                modifier = modifier
            ) {
                Text(
                    text = text,
                    color = Color(0xFFFDE68A),
                    fontSize = sizeSp.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
fun DynamicSceneGraphic(
    scene: SceneEntity?,
    modifier: Modifier = Modifier
) {
    val title = scene?.title ?: "Scene Visual"
    val prompt = scene?.visualPrompt ?: "Cinematic visual generation"
    val accentHex = scene?.accentColorHex ?: "#8B5CF6"
    val baseColor = try {
        Color(android.graphics.Color.parseColor(accentHex))
    } catch (e: Exception) {
        KishuPrimary
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        baseColor.copy(alpha = 0.55f),
                        Color(0xFF0F172A),
                        Color(0xFF05070D)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, baseColor.copy(alpha = 0.4f)),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "🎬 $title",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Text(
                text = prompt.take(120),
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                maxLines = 3
            )
        }
    }
}

fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d", min, sec)
}
