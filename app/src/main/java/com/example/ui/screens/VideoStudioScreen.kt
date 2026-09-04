package com.example.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatio
import com.example.data.model.SubtitlePosition
import com.example.data.model.SubtitleStyle
import com.example.data.model.VideoStyle
import com.example.data.model.VoiceLanguage
import com.example.data.model.VoiceTone
import com.example.ui.KishuViewModel
import com.example.ui.theme.KishuCardBorder
import com.example.ui.theme.KishuDarkSurface
import com.example.ui.theme.KishuDarkSurfaceVariant
import com.example.ui.theme.KishuPrimary
import com.example.ui.theme.KishuSecondary
import com.example.ui.theme.KishuTertiary

@Composable
fun VideoStudioScreen(
    viewModel: KishuViewModel,
    modifier: Modifier = Modifier
) {
    val form by viewModel.videoForm.collectAsState()
    val isGenerating by viewModel.isGeneratingVideo.collectAsState()
    val statusMsg by viewModel.generationStatusMessage.collectAsState()
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Brush.linearGradient(listOf(KishuPrimary, KishuSecondary)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Video Icon",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Kishu Video Studio",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Text-to-Video with Voiceover & Subtitles",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Unlimited Prompt Input Section
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = KishuDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, KishuCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Story & Visual Prompt (असीमित शब्द)",
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Magic Wand AI Enhancer
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = KishuPrimary.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, KishuPrimary.copy(alpha = 0.4f)),
                            modifier = Modifier.clickable { viewModel.enhanceVideoPrompt() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Enhance",
                                    tint = KishuPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AI Enhance",
                                    color = KishuPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = form.prompt,
                        onValueChange = { viewModel.updateVideoPrompt(it) },
                        placeholder = {
                            Text(
                                text = "Describe your full story or video scene without any length limit...\nExample: A majestic golden temple hidden in the Himalayas at sunrise, with floating lanterns, cinematic drone shot, sacred chanting, and vibrant dawn colors.",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp
                            )
                        },
                        trailingIcon = {
                            if (form.prompt.isNotBlank()) {
                                IconButton(onClick = { viewModel.updateVideoPrompt("") }) {
                                    Icon(Icons.Default.Clear, "Clear prompt", tint = Color(0xFF94A3B8))
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 220.dp)
                            .testTag("prompt_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KishuPrimary,
                            unfocusedBorderColor = KishuCardBorder,
                            focusedContainerColor = Color(0xFF0D1220),
                            unfocusedContainerColor = Color(0xFF0D1220),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Quick Prompt Inspiration Chips
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Quick Inspiration:", color = Color(0xFF64748B), fontSize = 11.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        InspirationChip("🌌 Space Odyssey") {
                            viewModel.loadTemplate(
                                "Cosmic journey through an iridescent nebula with futuristic starships and celestial supernovas.",
                                VideoStyle.CINEMATIC, 30, VoiceLanguage.HINDI
                            )
                        }
                        InspirationChip("🦊 3D Pixar Animation") {
                            viewModel.loadTemplate(
                                "A cute baby fox wearing a tiny astronaut backpack discovers a glowing magic mushroom in an enchanted forest.",
                                VideoStyle.CARTOON_3D, 20, VoiceLanguage.ENGLISH
                            )
                        }
                        InspirationChip("⚡ Cyberpunk 2077") {
                            viewModel.loadTemplate(
                                "Neo Tokyo cyberpunk streets with flying holographic cars, neon rain reflections, and cybernetic detectives.",
                                VideoStyle.CYBERPUNK, 60, VoiceLanguage.ENGLISH
                            )
                        }
                        InspirationChip("🪔 Ancient Mythic Varanasi") {
                            viewModel.loadTemplate(
                                "Sunset Ganga Aarti in Varanasi with thousands of floating golden diyas, sacred chants, and drone aerials.",
                                VideoStyle.FANTASY_MYTH, 60, VoiceLanguage.HINDI
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Video Duration Selector (Up to 30 Minutes!)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = KishuDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, KishuCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Video Duration (अवधि)",
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = KishuTertiary.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, KishuTertiary.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = formatDisplayDuration(form.durationSeconds),
                                color = KishuTertiary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Duration Preset Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DurationChip(15, "15s (Shorts)", form.durationSeconds == 15) { viewModel.updateVideoDuration(15) }
                        DurationChip(30, "30s (Reel)", form.durationSeconds == 30) { viewModel.updateVideoDuration(30) }
                        DurationChip(60, "1 Min", form.durationSeconds == 60) { viewModel.updateVideoDuration(60) }
                        DurationChip(180, "3 Min", form.durationSeconds == 180) { viewModel.updateVideoDuration(180) }
                        DurationChip(300, "5 Min", form.durationSeconds == 300) { viewModel.updateVideoDuration(300) }
                        DurationChip(600, "10 Min", form.durationSeconds == 600) { viewModel.updateVideoDuration(600) }
                        DurationChip(1800, "🌟 30 Min (Full Movie)", form.durationSeconds == 1800) { viewModel.updateVideoDuration(1800) }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Duration Slider
                    Slider(
                        value = form.durationSeconds.toFloat(),
                        onValueChange = { viewModel.updateVideoDuration(it.toInt()) },
                        valueRange = 10f..1800f,
                        steps = 59,
                        colors = SliderDefaults.colors(
                            thumbColor = KishuTertiary,
                            activeTrackColor = KishuTertiary,
                            inactiveTrackColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.testTag("duration_slider")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Video Style Selector
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = KishuDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, KishuCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Video Style & Type (वीडियो प्रकार)",
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VideoStyle.values().forEach { style ->
                            val isSelected = form.style == style
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) KishuPrimary.copy(alpha = 0.25f) else Color(0xFF0F172A),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) KishuPrimary else KishuCardBorder
                                ),
                                modifier = Modifier
                                    .clickable { viewModel.updateVideoStyle(style) }
                                    .testTag("style_${style.name}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = KishuPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = style.displayName,
                                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Aspect Ratio Selector
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = KishuDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, KishuCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Screen Ratio (अनुपात)",
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AspectRatio.values().forEach { ratio ->
                            val isSelected = form.aspectRatio == ratio
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) KishuSecondary.copy(alpha = 0.25f) else Color(0xFF0F172A),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) KishuSecondary else KishuCardBorder
                                ),
                                modifier = Modifier
                                    .clickable { viewModel.updateVideoAspectRatio(ratio) }
                                    .testTag("ratio_${ratio.name}")
                            ) {
                                Text(
                                    text = ratio.label,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Integrated Voiceover & Audio Engine (Multi-Language)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = KishuDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, KishuCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RecordVoiceOver, null, tint = KishuSecondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Voiceover Engine (वॉइसओवर)",
                                color = Color(0xFFE2E8F0),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Preview Voice button
                        Button(
                            onClick = { viewModel.previewVoice() },
                            colors = ButtonDefaults.buttonColors(containerColor = KishuSecondary.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, null, tint = KishuSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Listen", color = KishuSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Language Chips
                    Text(text = "Language (भाषा):", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        VoiceLanguage.values().forEach { lang ->
                            val isSelected = form.language == lang
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateVideoLanguage(lang) },
                                label = { Text("${lang.nativeName} (${lang.label})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = KishuSecondary,
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color(0xFF0F172A),
                                    labelColor = Color(0xFFCBD5E1)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Voice Tone Chips
                    Text(text = "Voice Tone (ध्वनि शैली):", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        VoiceTone.values().forEach { tone ->
                            val isSelected = form.voiceTone == tone
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateVideoVoiceTone(tone) },
                                label = { Text(tone.label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = KishuPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF0F172A),
                                    labelColor = Color(0xFFCBD5E1)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitles & SRT Screen Customization Section
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = KishuDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, KishuCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Subtitles, null, tint = KishuTertiary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Subtitles & SRT (सबटाइटल्स)",
                                color = Color(0xFFE2E8F0),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Switch(
                            checked = form.subtitleEnabled,
                            onCheckedChange = {
                                viewModel.updateSubtitleSettings(
                                    form.subtitleSizeSp,
                                    form.subtitlePosition,
                                    form.subtitleStyle,
                                    it
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = KishuPrimary
                            )
                        )
                    }

                    if (form.subtitleEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Subtitle Size Customization
                        Text(
                            text = "Screen Font Size: ${form.subtitleSizeSp} sp",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Slider(
                            value = form.subtitleSizeSp.toFloat(),
                            onValueChange = {
                                viewModel.updateSubtitleSettings(
                                    it.toInt(),
                                    form.subtitlePosition,
                                    form.subtitleStyle,
                                    form.subtitleEnabled
                                )
                            },
                            valueRange = 12f..30f,
                            colors = SliderDefaults.colors(
                                thumbColor = KishuTertiary,
                                activeTrackColor = KishuTertiary
                            )
                        )

                        // Subtitle Position
                        Text(text = "Screen Position:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SubtitlePosition.values().forEach { pos ->
                                val isSelected = form.subtitlePosition == pos
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.updateSubtitleSettings(
                                            form.subtitleSizeSp,
                                            pos,
                                            form.subtitleStyle,
                                            form.subtitleEnabled
                                        )
                                    },
                                    label = { Text(pos.label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KishuTertiary,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Subtitle Styling
                        Text(text = "Subtitle Style:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SubtitleStyle.values().forEach { sStyle ->
                                val isSelected = form.subtitleStyle == sStyle
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.updateSubtitleSettings(
                                            form.subtitleSizeSp,
                                            form.subtitlePosition,
                                            sStyle,
                                            form.subtitleEnabled
                                        )
                                    },
                                    label = { Text(sStyle.label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KishuPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Create Button
            Button(
                onClick = { viewModel.generateVideo() },
                enabled = !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("generate_video_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(KishuPrimary, KishuSecondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGenerating) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Creating Video Storyboard...",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Videocam, null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generate Video Storyboard & Clips",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (isGenerating && statusMsg.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = statusMsg,
                    color = KishuSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun InspirationChip(text: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, KishuCardBorder),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            color = Color(0xFFCBD5E1),
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun DurationChip(seconds: Int, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) KishuTertiary.copy(alpha = 0.25f) else Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) KishuTertiary else KishuCardBorder),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

fun formatDisplayDuration(seconds: Int): String {
    val min = seconds / 60
    val sec = seconds % 60
    return if (min > 0) {
        if (sec > 0) "${min}m ${sec}s" else "${min} Minutes"
    } else {
        "${sec} Seconds"
    }
}
