package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectType
import com.example.ui.KishuViewModel
import com.example.ui.StudioTab
import com.example.ui.theme.KishuCardBorder
import com.example.ui.theme.KishuDarkSurface
import com.example.ui.theme.KishuPrimary
import com.example.ui.theme.KishuSecondary
import com.example.ui.theme.KishuTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectsScreen(
    viewModel: KishuViewModel,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.allProjects.collectAsState()
    var selectedFilter by remember { mutableStateOf<ProjectType?>(null) }

    val filtered = remember(projects, selectedFilter) {
        if (selectedFilter == null) projects else projects.filter { it.type == selectedFilter }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        Brush.linearGradient(listOf(KishuTertiary, KishuPrimary)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "My Studio Projects",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${projects.size} Created Productions",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { selectedFilter = null },
                label = { Text("All (${projects.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = KishuPrimary,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedFilter == ProjectType.VIDEO,
                onClick = { selectedFilter = ProjectType.VIDEO },
                label = { Text("Videos") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = KishuPrimary,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedFilter == ProjectType.IMAGE,
                onClick = { selectedFilter = ProjectType.IMAGE },
                label = { Text("Images") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = KishuPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No creations found",
                        color = Color(0xFF94A3B8),
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Start by generating a video in the Video Studio!",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { project ->
                    ProjectItemCard(
                        project = project,
                        onOpenEditor = {
                            viewModel.loadProjectIntoEditor(project.id)
                            viewModel.selectTab(StudioTab.EDITOR)
                        },
                        onDelete = { viewModel.deleteProject(project.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectItemCard(
    project: ProjectEntity,
    onOpenEditor: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(project.createdAt) {
        val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        sdf.format(Date(project.createdAt))
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = KishuDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, KishuCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenEditor() }
            .testTag("project_item_${project.id}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail / Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (project.type == ProjectType.VIDEO) Color(0xFF1E1B4B) else Color(0xFF0F172A)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (project.previewImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = project.previewImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = if (project.type == ProjectType.VIDEO) Icons.Default.Videocam else Icons.Default.Image,
                        contentDescription = null,
                        tint = if (project.type == ProjectType.VIDEO) KishuPrimary else KishuSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (project.type == ProjectType.VIDEO) {
                        "${project.durationSeconds}s • ${project.style} • ${project.language}"
                    } else {
                        "Graphic • ${project.style} • ${project.aspectRatio}"
                    },
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateStr,
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )
            }

            // Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (project.type == ProjectType.VIDEO) {
                    IconButton(
                        onClick = onOpenEditor,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, "Open in Editor", tint = KishuSecondary)
                    }
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
