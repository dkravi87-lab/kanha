package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = KishuPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A1D96),
    onPrimaryContainer = KishuPrimaryGlow,
    secondary = KishuSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = KishuSecondaryGlow,
    tertiary = KishuTertiary,
    onTertiary = Color.Black,
    background = KishuDarkBg,
    onBackground = KishuTextPrimary,
    surface = KishuDarkSurface,
    onSurface = KishuTextPrimary,
    surfaceVariant = KishuDarkSurfaceVariant,
    onSurfaceVariant = KishuTextSecondary,
    outline = KishuCardBorder
  )

private val LightColorScheme =
  darkColorScheme(
    primary = KishuPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A1D96),
    onPrimaryContainer = KishuPrimaryGlow,
    secondary = KishuSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = KishuSecondaryGlow,
    tertiary = KishuTertiary,
    onTertiary = Color.Black,
    background = KishuDarkBg,
    onBackground = KishuTextPrimary,
    surface = KishuDarkSurface,
    onSurface = KishuTextPrimary,
    surfaceVariant = KishuDarkSurfaceVariant,
    onSurfaceVariant = KishuTextSecondary,
    outline = KishuCardBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to Kishu Studio dark cinema theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

