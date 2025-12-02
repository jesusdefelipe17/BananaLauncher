package com.example.afo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.afo.themes.ThemeManager

@Composable
fun AFOTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeManager = ThemeManager.getInstance(context)
    val currentTheme by themeManager.currentTheme.collectAsState()

    // Crear ColorScheme dinámico basado en el tema actual
    val colorScheme = if (currentTheme.isDark) {
        darkColorScheme(
            primary = currentTheme.primaryColor,
            onPrimary = Color.White,
            primaryContainer = currentTheme.primaryColor.copy(alpha = 0.3f),
            onPrimaryContainer = currentTheme.textPrimary,

            secondary = currentTheme.secondaryColor,
            onSecondary = Color.White,
            secondaryContainer = currentTheme.secondaryColor.copy(alpha = 0.3f),
            onSecondaryContainer = currentTheme.textSecondary,

            tertiary = currentTheme.accentColor,
            onTertiary = Color.White,

            background = currentTheme.backgroundColor,
            onBackground = currentTheme.textPrimary,

            surface = currentTheme.surfaceColor,
            onSurface = currentTheme.textPrimary,
            surfaceVariant = currentTheme.cardColor,
            onSurfaceVariant = currentTheme.textSecondary,

            error = Color(0xFFCF6679),
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = currentTheme.primaryColor,
            onPrimary = Color.White,
            primaryContainer = currentTheme.primaryColor.copy(alpha = 0.1f),
            onPrimaryContainer = currentTheme.textPrimary,

            secondary = currentTheme.secondaryColor,
            onSecondary = Color.White,
            secondaryContainer = currentTheme.secondaryColor.copy(alpha = 0.1f),
            onSecondaryContainer = currentTheme.textSecondary,

            tertiary = currentTheme.accentColor,
            onTertiary = Color.White,

            background = currentTheme.backgroundColor,
            onBackground = currentTheme.textPrimary,

            surface = currentTheme.surfaceColor,
            onSurface = currentTheme.textPrimary,
            surfaceVariant = currentTheme.cardColor,
            onSurfaceVariant = currentTheme.textSecondary,

            error = Color(0xFFB00020),
            onError = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}