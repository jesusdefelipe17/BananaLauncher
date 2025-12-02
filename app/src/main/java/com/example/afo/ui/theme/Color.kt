package com.example.afo.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.afo.themes.ThemeManager

// Colores principales - Tema Oscuro inspirado en PS5 (valores por defecto)
val DarkBackground = Color(0xFF0A0E27)
val DarkSurface = Color(0xFF151B3D)
val DarkSurfaceVariant = Color(0xFF1E2749)
val CardBackground = Color(0xFF1A2142)

// Acentos
val PrimaryBlue = Color(0xFF4A90E2)
val PrimaryBlueDark = Color(0xFF2E5F9E)
val AccentPurple = Color(0xFF9B4DFF)
val AccentPink = Color(0xFFE91E63)

// Estados
val FavoriteGold = Color(0xFFFFB300)
val SuccessGreen = Color(0xFF4CAF50)
val ErrorRed = Color(0xFFE53935)

// Textos
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0B8D4)
val TextTertiary = Color(0xFF6E7AA3)

// Overlay
val OverlayDark = Color(0xCC000000)
val OverlayLight = Color(0x66FFFFFF)

// Funciones Composables para obtener colores dinámicos del tema actual
@Composable
fun getDynamicBackground(): Color {
    val context = LocalContext.current
    val themeManager = ThemeManager.getInstance(context)
    val theme by themeManager.currentTheme.collectAsState()
    return theme.backgroundColor
}

@Composable
fun getDynamicSurface(): Color {
    val context = LocalContext.current
    val themeManager = ThemeManager.getInstance(context)
    val theme by themeManager.currentTheme.collectAsState()
    return theme.surfaceColor
}

@Composable
fun getDynamicCardBackground(): Color {
    val context = LocalContext.current
    val themeManager = ThemeManager.getInstance(context)
    val theme by themeManager.currentTheme.collectAsState()
    return theme.cardColor
}

@Composable
fun getDynamicPrimaryBlue(): Color {
    val context = LocalContext.current
    val themeManager = ThemeManager.getInstance(context)
    val theme by themeManager.currentTheme.collectAsState()
    return theme.primaryColor
}

@Composable
fun getDynamicTextPrimary(): Color {
    val context = LocalContext.current
    val themeManager = ThemeManager.getInstance(context)
    val theme by themeManager.currentTheme.collectAsState()
    return theme.textPrimary
}

@Composable
fun getDynamicTextSecondary(): Color {
    val context = LocalContext.current
    val themeManager = ThemeManager.getInstance(context)
    val theme by themeManager.currentTheme.collectAsState()
    return theme.textSecondary
}

