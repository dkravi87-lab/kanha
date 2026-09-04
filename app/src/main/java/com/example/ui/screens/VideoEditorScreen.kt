package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatio
import com.example.data.model.CameraMotion
import com.example.data.model.SceneEntity
import com.example.data.model.SubtitlePosition
import com.example.data.model.SubtitleStyle
import com.example.data.model.TransitionType
import com.example.ui.KishuViewModel
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.KishuCardBorder
import com.example.ui.theme.KishuDarkSurface
import com.example.ui.theme.KishuDarkSurfaceVariant
import com.example.ui.theme.KishuPrimary
import com.example.ui.theme.KishuSecondary
import com.example.ui.theme.KishuTertiary

@Composable
fun VideoEditorScreen(
    viewModel: KishuViewModel,
    modifier: Modifier = Modifier
) {
    val activeProject by viewModel.activeProject.collectAsState()
    val scenes by viewModel.activeScenes.collectAsState()
    val selectedScene by viewModel.selectedSceneForEdit.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showSrtDialog by remember { mutableStateOf(false) }

    if (activeProject == null) {
        // Empty State: Prompt user to generate or select a project
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = KishuPrimary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Active Video Loaded",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Generate a video in the Video Studio or select a project from 'My Projects' to refine clips, voiceover, and subtitles.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.selectTab(com.example.ui.StudioTab.VIDEO) },
                    colors = ButtonDefaults.buttonColors(containerColor = KishuPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Go to Video Studio")
                }
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Top Info & Title Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activeProject?.title ?: "Video Project",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "${scenes.size} Clips • Total ${scenes.sumOf { it.durationSeconds }}s • ${activeProject?.style}",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }

            // Export SRT Button
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = KishuTertiary.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, KishuTertiary.copy(alpha = 0.4f)),
                modifier = Modifier.clickable { showSrtDialog = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Subtitles, null, tint = KishuTertiary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View SRT", color = KishuTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 1. Live Interactive Video Player
        VideoPlayerView(
            controller = viewModel.playerController,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Timeline Strip (Clips Scrubber)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = KishuDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, KishuCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Timeline Clips (${scenes.size} Scenes)",
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Add Clip Button
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = KishuPrimary.copy(alpha = 0.2f),
                        modifier = Modifier.clickable { viewModel.addClipToTimeline() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.Add, null, tint = KishuPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Add Clip", color = KishuPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Horizontal list of clip cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    scenes.forEachIndexed { index, scene ->
                        val isSelected = selectedScene?.id == scene.id
                        val isPlayingCurrent = viewModel.playerController.currentSceneIndex == index

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF2E1065) else Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) KishuPrimary else if (isPlayingCurrent) KishuSecondary else KishuCardBorder
                            ),
                            modifier = Modifier
                                .width(120.dp)
                                .clickable { viewModel.selectSceneForEdit(scene) }
                                .testTag("timeline_clip_$index")
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "#${index + 1}",
                                        color = if (isPlayingCurrent) KishuSecondary else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${scene.durationSeconds}s",
                                        color = KishuTertiary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = scene.title,
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = scene.cameraMotion.replace("_", " "),
                                    color = Color(0xFF64748B),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Clip Refinement & Inspector Panel
        if (selectedScene != null) {
            ClipInspectorCard(
                scene = selectedScene!!,
                onUpdate = { title, narration, subtitle, dur, cam, trans ->
                    viewModel.updateSelectedScene(title, narration, subtitle, dur, cam, trans)
                },
                onDelete = { viewModel.deleteSelectedClip() },
                onPreviewVoice = { text -> viewModel.previewVoice(text) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Subtitle & Screen Setup Customization
        SubtitleAndScreenCustomizer(
            currentSize = viewModel.playerController.subtitleSizeSp,
            currentPosition = viewModel.playerController.subtitlePosition,
            currentStyle = viewModel.playerController.subtitleStyle,
            currentRatio = viewModel.playerController.aspectRatio,
            onUpdateSubtitle = { size, pos, style ->
                viewModel.updateEditorSubtitleAppearance(size, pos, style)
            },
            onUpdateRatio = { ratio ->
                viewModel.updateEditorAspectRatio(ratio)
            }
        )

        Spacer(modifier = Modifier.height(40.dp))
    }

    // SRT Export / View Dialog
    if (showSrtDialog) {
        val srtText = activeProject?.srtContent ?: ""
        AlertDialog(
            onDismissRequest = { showSrtDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Subtitles, null, tint = KishuTertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SRT Subtitles File")
                }
            },
            text = {
                Column {
                    Text(
                        text = "Standard SubRip (.srt) format generated in ${activeProject?.language}:",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, KishuCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(8.dp)
                        ) {
                            Text(
                                text = srtText.ifBlank { "No subtitles generated." },
                                color = Color(0xFFF1F5F9),
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("SRT Subtitles", srtText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "SRT Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                        showSrtDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KishuTertiary)
                ) {
                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy SRT", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSrtDialog = false }) {
                    Text("Close", color = Color(0xFF94A3B8))
                }
            },
            containerColor = KishuDarkSurface
        )
    }
}

@Composable
fun ClipInspectorCard(
    scene: SceneEntity,
    onUpdate: (String, String, String, Int, CameraMotion, TransitionType) -> Unit,
    onDelete: () -> Unit,
    onPreviewVoice: (String) -> Unit
) {
    var title by remember(scene.id) { mutableStateOf(scene.title) }
    var narration by remember(scene.id) { mutableStateOf(scene.narrationText) }
    var subtitle by remember(scene.id) { mutableStateOf(scene.subtitleText) }
    var duration by remember(scene.id) { mutableIntStateOf(scene.durationSeconds) }
    var cameraMotion by remember(scene.id) {
        mutableStateOf(
            try { CameraMotion.valueOf(scene.cameraMotion) } catch (e: Exception) { CameraMotion.ZOOM_IN }
        )
    }
    var transition by remember(scene.id) {
        mutableStateOf(
            try { TransitionType.valueOf(scene.transitionType) } catch (e: Exception) { TransitionType.CROSSFADE }
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = KishuDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, KishuCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Scene Title & Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, null, tint = KishuPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Refine Clip #${scene.sceneIndex}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, "Delete clip", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clip Title
            Text("Clip Title:", color = Color(0xFF94A3B8), fontSize = 11.sp)
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    onUpdate(title, narration, subtitle, duration, cameraMotion, transition)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KishuPrimary,
                    unfocusedBorderColor = KishuCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Narration Voiceover Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Voiceover Script (आवाज़ संवाद):", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Text(
                    text = "🔊 Test Speak",
                    color = KishuSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onPreviewVoice(narration) }
                )
            }
            OutlinedTextField(
                value = narration,
                onValueChange = {
                    narration = it
                    onUpdate(title, narration, subtitle, duration, cameraMotion, transition)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KishuSecondary,
                    unfocusedBorderColor = KishuCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle Line
            Text("Subtitle Text (सबटाइटल पंक्ति):", color = Color(0xFF94A3B8), fontSize = 11.sp)
            OutlinedTextField(
                value = subtitle,
                onValueChange = {
                    subtitle = it
                    onUpdate(title, narration, subtitle, duration, cameraMotion, transition)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KishuTertiary,
                    unfocusedBorderColor = KishuCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Duration Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Clip Duration:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Text("${duration}s", color = KishuTertiary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = duration.toFloat(),
                onValueChange = {
                    duration = it.toInt()
                    onUpdate(title, narration, subtitle, duration, cameraMotion, transition)
                },
                valueRange = 1f..60f,
                colors = SliderDefaults.colors(
                    thumbColor = KishuTertiary,
                    activeTrackColor = KishuTertiary
                )
            )

            // Camera Motion Direction
            Text("Camera Motion (कैमरा मूवमेंट):", color = Color(0xFF94A3B8), fontSize = 11.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CameraMotion.values().forEach { cam ->
                    val isSelected = cameraMotion == cam
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            cameraMotion = cam
                            onUpdate(title, narration, subtitle, duration, cameraMotion, transition)
                        },
                        label = { Text(cam.label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KishuPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Transition Type
            Text("Scene Transition (ट्रांज़िशन):", color = Color(0xFF94A3B8), fontSize = 11.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TransitionType.values().forEach { trans ->
                    val isSelected = transition == trans
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            transition = trans
                            onUpdate(title, narration, subtitle, duration, cameraMotion, transition)
                        },
                        label = { Text(trans.label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KishuSecondary,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SubtitleAndScreenCustomizer(
    currentSize: Int,
    currentPosition: SubtitlePosition,
    currentStyle: SubtitleStyle,
    currentRatio: AspectRatio,
    onUpdateSubtitle: (Int, SubtitlePosition, SubtitleStyle) -> Unit,
    onUpdateRatio: (AspectRatio) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = KishuDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, KishuCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Screen Customization & Subtitle Set (स्क्रीन सेट व आकार)",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Screen Aspect Ratio Switcher
            Text("Output Framing Ratio:", color = Color(0xFF94A3B8), fontSize = 11.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AspectRatio.values().forEach { ratio ->
                    val isSelected = currentRatio == ratio
                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdateRatio(ratio) },
                        label = { Text(ratio.label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KishuSecondary,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle Font Size Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtitle Font Size:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Text("${currentSize} sp", color = KishuTertiary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = currentSize.toFloat(),
                onValueChange = { onUpdateSubtitle(it.toInt(), currentPosition, currentStyle) },
                valueRange = 12f..30f,
                colors = SliderDefaults.colors(thumbColor = KishuTertiary, activeTrackColor = KishuTertiary)
            )

            // Subtitle Position
            Text("Subtitle Position on Screen:", color = Color(0xFF94A3B8), fontSize = 11.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SubtitlePosition.values().forEach { pos ->
                    val isSelected = currentPosition == pos
                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdateSubtitle(currentSize, pos, currentStyle) },
                        label = { Text(pos.label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KishuPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle Styling
            Text("Subtitle Visual Style:", color = Color(0xFF94A3B8), fontSize = 11.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SubtitleStyle.values().forEach { style ->
                    val isSelected = currentStyle == style
                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdateSubtitle(currentSize, currentPosition, style) },
                        label = { Text(style.label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KishuTertiary,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
        }
    }
}
