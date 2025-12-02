package com.example.afo.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val EmuLauncherColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = TextPrimary,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = TextPrimary,

    secondary = AccentPurple,
    onSecondary = TextPrimary,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = TextSecondary,

    tertiary = AccentPink,
    onTertiary = TextPrimary,

    background = DarkBackground,
    onBackground = TextPrimary,

    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,

    error = ErrorRed,
    onError = TextPrimary
)

@Composable
fun AFOTheme(
    darkTheme: Boolean = true, // Siempre modo oscuro para este launcher
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EmuLauncherColorScheme,
        typography = Typography,
        content = content
    )
}