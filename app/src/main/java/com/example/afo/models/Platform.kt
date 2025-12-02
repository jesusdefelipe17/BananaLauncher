package com.example.afo.models

import androidx.compose.ui.graphics.Color

enum class Platform(
    val displayName: String,
    val extensions: List<String>,
    val color: Color
) {
    // Nintendo Consoles
    GB("Game Boy", listOf("gb"), Color(0xFF8BAC0F)),
    GBC("Game Boy Color", listOf("gbc"), Color(0xFF9B30FF)),
    GBA("Game Boy Advance", listOf("gba"), Color(0xFF6B4C9A)),
    NES("Nintendo NES", listOf("nes"), Color(0xFFE60012)),
    SNES("Super Nintendo", listOf("smc", "sfc", "snes"), Color(0xFF8F8F8F)),
    N64("Nintendo 64", listOf("n64", "z64", "v64"), Color(0xFF0E4C92)),
    NDS("Nintendo DS", listOf("nds"), Color(0xFFD12228)),
    SWITCH("Nintendo Switch", listOf("nsp", "xci", "nsz", "xcz"), Color(0xFFE60012)),

    // PlayStation Consoles
    PSX("PlayStation 1", listOf("bin", "cue", "img", "pbp", "chd"), Color(0xFF003791)),
    PSP("PlayStation Portable", listOf("cso"), Color(0xFF000000)),  // ISO se maneja especialmente
    PS2("PlayStation 2", listOf("chd"), Color(0xFF003791)),

    PS3("PlayStation 3", listOf("pck"), Color(0xFF000000)),

    // Sega Consoles
    MD("Sega Genesis", listOf("md", "gen", "smd"), Color(0xFF0089CF)),

    // Arcade
    ARCADE("Arcade", listOf("zip", "7z"), Color(0xFFFF6B00)),

    UNKNOWN("Unknown", emptyList(), Color(0xFF666666));

    companion object {
        fun fromExtension(extension: String): Platform {
            val ext = extension.lowercase()

            // Casos especiales primero
            return when (ext) {
                "cso" -> PSP
                "nsp", "xci", "nsz", "xcz" -> SWITCH
                "bin", "cue", "pbp", "chd" -> PSX
                else -> {
                    // Para otros casos, buscar en todas las plataformas
                    entries.firstOrNull { platform ->
                        platform.extensions.any { it.equals(ext, ignoreCase = true) }
                    } ?: UNKNOWN
                }
            }
        }

        // Método especial para detectar ISO basándose en la ruta y nombre
        fun fromFile(fileName: String, extension: String, filePath: String): Platform {
            val ext = extension.lowercase()
            val path = filePath.lowercase()
            val name = fileName.lowercase()

            // Para archivos ISO, intentar detectar por contexto
            if (ext == "iso") {
                return when {
                    // PSP suele tener carpetas con "psp" en el nombre
                    path.contains("psp") ||
                    path.contains("ppsspp") ||
                    name.contains("[psp]") ||
                    name.contains("(psp)") -> PSP

                    // Switch puede tener ISOs en algunas configuraciones
                    path.contains("switch") ||
                    path.contains("yuzu") ||
                    path.contains("ryujinx") -> SWITCH

                    // PS2
                    path.contains("ps2") ||
                    path.contains("pcsx2") -> PS2

                    // Por defecto, ISO es PSX (más común)
                    else -> PSX
                }
            }

            // Para otros casos, usar el método normal
            return fromExtension(ext)
        }
    }
}

