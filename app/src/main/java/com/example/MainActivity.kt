package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.KishuViewModel
import com.example.ui.StudioTab
import com.example.ui.screens.ImageStudioScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.VideoEditorScreen
import com.example.ui.screens.VideoStudioScreen
import com.example.ui.theme.KishuCardBorder
import com.example.ui.theme.KishuDarkBackground
import com.example.ui.theme.KishuDarkSurface
import com.example.ui.theme.KishuPrimary
import com.example.ui.theme.KishuSecondary
import com.example.ui.theme.KishuTertiary
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: KishuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                KishuMainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KishuMainApp(viewModel: KishuViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val toastMsg by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(KishuDarkBackground),
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            color = KishuPrimary.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, KishuPrimary.copy(alpha = 0.4f)),
                            modifier = Modifier.padding(end = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = KishuPrimary,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(18.dp)
                            )
                        }
                        androidx.compose.foundation.layout.Column {
                            Text(
                                text = "Kishu • किशु AI",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Video & Image Studio",
                                fontSize = 11.sp,
                                color = KishuSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KishuDarkSurface,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = KishuDarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == StudioTab.VIDEO,
                    onClick = { viewModel.selectTab(StudioTab.VIDEO) },
                    icon = { Icon(Icons.Default.Videocam, "Video Studio") },
                    label = { Text("Video", fontSize = 11.sp, fontWeight = if (currentTab == StudioTab.VIDEO) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = KishuPrimary,
                        indicatorColor = KishuPrimary.copy(alpha = 0.35f),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("tab_video")
                )

                NavigationBarItem(
                    selected = currentTab == StudioTab.IMAGE,
                    onClick = { viewModel.selectTab(StudioTab.IMAGE) },
                    icon = { Icon(Icons.Default.Image, "Image Studio") },
                    label = { Text("Image", fontSize = 11.sp, fontWeight = if (currentTab == StudioTab.IMAGE) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = KishuSecondary,
                        indicatorColor = KishuSecondary.copy(alpha = 0.35f),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("tab_image")
                )

                NavigationBarItem(
                    selected = currentTab == StudioTab.EDITOR,
                    onClick = { viewModel.selectTab(StudioTab.EDITOR) },
                    icon = { Icon(Icons.Default.Movie, "Clip Editor") },
                    label = { Text("Editor", fontSize = 11.sp, fontWeight = if (currentTab == StudioTab.EDITOR) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = KishuTertiary,
                        indicatorColor = KishuTertiary.copy(alpha = 0.35f),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("tab_editor")
                )

                NavigationBarItem(
                    selected = currentTab == StudioTab.PROJECTS,
                    onClick = { viewModel.selectTab(StudioTab.PROJECTS) },
                    icon = { Icon(Icons.Default.VideoLibrary, "Projects") },
                    label = { Text("Projects", fontSize = 11.sp, fontWeight = if (currentTab == StudioTab.PROJECTS) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = KishuPrimary,
                        indicatorColor = KishuPrimary.copy(alpha = 0.35f),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("tab_projects")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = KishuDarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .background(KishuDarkBackground)
        ) {
            when (currentTab) {
                StudioTab.VIDEO -> VideoStudioScreen(viewModel = viewModel)
                StudioTab.IMAGE -> ImageStudioScreen(viewModel = viewModel)
                StudioTab.EDITOR -> VideoEditorScreen(viewModel = viewModel)
                StudioTab.PROJECTS -> ProjectsScreen(viewModel = viewModel)
            }
        }
    }
}

// Keep Greeting for backward-compatibility with screenshot unit tests
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
