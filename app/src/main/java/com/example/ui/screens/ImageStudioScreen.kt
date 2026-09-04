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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AspectRatio
import com.example.data.model.VideoStyle
import com.example.ui.KishuViewModel
import com.example.ui.theme.KishuCardBorder
import com.example.ui.theme.KishuDarkSurface
import com.example.ui.theme.KishuPrimary
import com.example.ui.theme.KishuSecondary

@Composable
fun ImageStudioScreen(
    viewModel: KishuViewModel,
    modifier: Modifier = Modifier
) {
    val form by viewModel.imageForm.collectAsState()
    val isGenerating by viewModel.isGeneratingImage.collectAsState()
    val lastImage by viewModel.lastGeneratedImage.collectAsState()
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Brush.linearGradient(listOf(KishuSecondary, KishuPrimary)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Image Icon",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Kishu Image Studio",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "High-Quality Realistic Graphics from Prompts",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Unlimited Prompt Section
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = KishuDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, KishuCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Graphic Prompt (असीमित शब्द)",
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = form.prompt,
                        onValueChange = { viewModel.updateImagePrompt(it) },
                        placeholder = {
                            Text(
                                text = "Enter descriptive prompt for realistic, 8K ultra-detailed graphics...\nExample: A crystal dragon flying over a sunset ocean, water reflections, volumetric lighting, photorealistic.",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp
                            )
                        },
                        trailingIcon = {
                            if (form.prompt.isNotBlank()) {
                                IconButton(onClick = { viewModel.updateImagePrompt("") }) {
                                    Icon(Icons.Default.Clear, "Clear prompt", tint = Color(0xFF94A3B8))
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 180.dp)
                            .testTag("image_prompt_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KishuSecondary,
                            unfocusedBorderColor = KishuCardBorder,
                            focusedContainerColor = Color(0xFF0D1220),
                            unfocusedContainerColor = Color(0xFF0D1220),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Inspiration Prompts
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Presets:", color = Color(0xFF64748B), fontSize = 11.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        InspirationChip("🐅 Royal Bengal Tiger in Rain") {
                            viewModel.updateImagePrompt("A majestic Royal Bengal Tiger walking through a mist-covered jungle in monsoon rain, 8k photorealistic, droplets on fur, intense golden eyes.")
                        }
                        InspirationChip("🚀 Cyberpunk Hovercar") {
                            viewModel.updateImagePrompt("Futuristic sleek flying speeder soaring between neon-lit skyscrapers of Neo Mumbai, cinematic anamorphic lens flare.")
                        }
                        InspirationChip("🌺 Ghibli Valley") {
                            viewModel.updateImagePrompt("A serene Japanese valley with lush green hills, blooming cherry blossoms, gentle river, painted Ghibli anime style, warm sunlight.")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Style Selector
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = KishuDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, KishuCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Art Style (चित्र शैली)",
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
                                color = if (isSelected) KishuSecondary.copy(alpha = 0.25f) else Color(0xFF0F172A),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) KishuSecondary else KishuCardBorder
                                ),
                                modifier = Modifier.clickable { viewModel.updateImageStyle(style) }
                            ) {
                                Text(
                                    text = style.displayName,
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

            Spacer(modifier = Modifier.height(14.dp))

            // Ratio Selector
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = KishuDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, KishuCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Aspect Ratio (आकार)",
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
                                color = if (isSelected) KishuPrimary.copy(alpha = 0.25f) else Color(0xFF0F172A),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) KishuPrimary else KishuCardBorder
                                ),
                                modifier = Modifier.clickable { viewModel.updateImageAspectRatio(ratio) }
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

            Spacer(modifier = Modifier.height(20.dp))

            // Generate Button
            Button(
                onClick = { viewModel.generateImage() },
                enabled = !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_image_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = androidx.compose.foundation.layout.PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(KishuSecondary, KishuPrimary))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGenerating) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rendering High-Quality Graphic...", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Graphic Artwork", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Output Display
            if (lastImage != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = KishuDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, KishuCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Generated Graphic Output",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(form.aspectRatio.ratioFloat)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                        ) {
                            if (lastImage?.localFilePath != null) {
                                AsyncImage(
                                    model = lastImage?.localFilePath,
                                    contentDescription = "Generated art",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = lastImage?.description ?: "",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
