package com.example.afo.themes

import android.content.Context
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppTheme(
    val id: String,
    val name: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val cardColor: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentColor: Color,
    val isDark: Boolean
)

object ThemePresets {
    val DARK_BLUE = AppTheme(
        id = "dark_blue",
        name = "Azul Oscuro (Predeterminado)",
        primaryColor = Color(0xFF4A90E2),
        secondaryColor = Color(0xFF6B5CA5),
        backgroundColor = Color(0xFF0A0E27),
        surfaceColor = Color(0xFF1A1F3A),
        cardColor = Color(0xFF252B48),
        textPrimary = Color(0xFFE8E8E8),
        textSecondary = Color(0xFFB0B0B0),
        accentColor = Color(0xFFFFD700),
        isDark = true
    )

    val CYBERPUNK = AppTheme(
        id = "cyberpunk",
        name = "Cyberpunk",
        primaryColor = Color(0xFFFF00FF),
        secondaryColor = Color(0xFF00FFFF),
        backgroundColor = Color(0xFF0D0221),
        surfaceColor = Color(0xFF1A0B2E),
        cardColor = Color(0xFF2E1A47),
        textPrimary = Color(0xFFF0F0F0),
        textSecondary = Color(0xFFB8B8D1),
        accentColor = Color(0xFFFFFF00),
        isDark = true
    )

    val NEON_GREEN = AppTheme(
        id = "neon_green",
        name = "Verde Neón",
        primaryColor = Color(0xFF39FF14),
        secondaryColor = Color(0xFF00FF41),
        backgroundColor = Color(0xFF0A0F0D),
        surfaceColor = Color(0xFF111C18),
        cardColor = Color(0xFF1A2F26),
        textPrimary = Color(0xFFE0FFE0),
        textSecondary = Color(0xFFB0D0B0),
        accentColor = Color(0xFFFFFF00),
        isDark = true
    )

    val SUNSET = AppTheme(
        id = "sunset",
        name = "Atardecer",
        primaryColor = Color(0xFFFF6B35),
        secondaryColor = Color(0xFFFF9B42),
        backgroundColor = Color(0xFF1A0D0A),
        surfaceColor = Color(0xFF2D1810),
        cardColor = Color(0xFF3D2318),
        textPrimary = Color(0xFFFFE8D6),
        textSecondary = Color(0xFFD4A88C),
        accentColor = Color(0xFFFFD700),
        isDark = true
    )

    val OCEAN = AppTheme(
        id = "ocean",
        name = "Océano",
        primaryColor = Color(0xFF00B4D8),
        secondaryColor = Color(0xFF0077B6),
        backgroundColor = Color(0xFF001219),
        surfaceColor = Color(0xFF003049),
        cardColor = Color(0xFF004E73),
        textPrimary = Color(0xFFE0F4FF),
        textSecondary = Color(0xFFB0D5E8),
        accentColor = Color(0xFF48CAE4),
        isDark = true
    )

    val PURPLE_HAZE = AppTheme(
        id = "purple_haze",
        name = "Neblina Púrpura",
        primaryColor = Color(0xFF9D4EDD),
        secondaryColor = Color(0xFF7209B7),
        backgroundColor = Color(0xFF10002B),
        surfaceColor = Color(0xFF240046),
        cardColor = Color(0xFF3C096C),
        textPrimary = Color(0xFFF0E7FF),
        textSecondary = Color(0xFFC8B6D6),
        accentColor = Color(0xFFE0AAFF),
        isDark = true
    )

    val RETRO = AppTheme(
        id = "retro",
        name = "Retro Gaming",
        primaryColor = Color(0xFFFF3864),
        secondaryColor = Color(0xFF7B2CBF),
        backgroundColor = Color(0xFF0F0A0A),
        surfaceColor = Color(0xFF2D1B2E),
        cardColor = Color(0xFF3E2A47),
        textPrimary = Color(0xFFFFF1F1),
        textSecondary = Color(0xFFD1B7D3),
        accentColor = Color(0xFF00FFB3),
        isDark = true
    )

    val LIGHT_MODE = AppTheme(
        id = "light",
        name = "Modo Claro",
        primaryColor = Color(0xFF1976D2),
        secondaryColor = Color(0xFF0D47A1),
        backgroundColor = Color(0xFFF5F5F5),
        surfaceColor = Color(0xFFFFFFFF),
        cardColor = Color(0xFFFFFFFF),
        textPrimary = Color(0xFF212121),
        textSecondary = Color(0xFF757575),
        accentColor = Color(0xFFFF9800),
        isDark = false
    )

    val MINT = AppTheme(
        id = "mint",
        name = "Menta Fresca",
        primaryColor = Color(0xFF2DD4BF),
        secondaryColor = Color(0xFF14B8A6),
        backgroundColor = Color(0xFFECFDF5),
        surfaceColor = Color(0xFFFFFFFF),
        cardColor = Color(0xFFF0FDFA),
        textPrimary = Color(0xFF134E4A),
        textSecondary = Color(0xFF5E7C78),
        accentColor = Color(0xFFF59E0B),
        isDark = false
    )

    val CHERRY_BLOSSOM = AppTheme(
        id = "cherry",
        name = "Cerezo",
        primaryColor = Color(0xFFEC4899),
        secondaryColor = Color(0xFFDB2777),
        backgroundColor = Color(0xFFFDF2F8),
        surfaceColor = Color(0xFFFFFFFF),
        cardColor = Color(0xFFFCE7F3),
        textPrimary = Color(0xFF831843),
        textSecondary = Color(0xFF9F1239),
        accentColor = Color(0xFFF43F5E),
        isDark = false
    )

    fun getAllThemes(): List<AppTheme> = listOf(
        DARK_BLUE,
        CYBERPUNK,
        NEON_GREEN,
        SUNSET,
        OCEAN,
        PURPLE_HAZE,
        RETRO,
        LIGHT_MODE,
        MINT,
        CHERRY_BLOSSOM
    )
}

class ThemeManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _currentTheme = MutableStateFlow(loadTheme())
    val currentTheme = _currentTheme.asStateFlow()

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
        prefs.edit().putString("selected_theme_id", theme.id).apply()
    }

    fun getThemeById(id: String): AppTheme {
        return ThemePresets.getAllThemes().find { it.id == id } ?: ThemePresets.DARK_BLUE
    }

    private fun loadTheme(): AppTheme {
        val themeId = prefs.getString("selected_theme_id", "dark_blue") ?: "dark_blue"
        return getThemeById(themeId)
    }

    companion object {
        @Volatile
        private var instance: ThemeManager? = null

        fun getInstance(context: Context): ThemeManager {
            return instance ?: synchronized(this) {
                instance ?: ThemeManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

