package com.example.afo

import android.content.Context
import android.os.Environment
import com.example.afo.models.Platform
import com.example.afo.models.RomFile
import java.io.File

class RomScanner(private val context: Context) {

    private val defaultSearchPaths = listOf(
        "${Environment.getExternalStorageDirectory()}/Download/roms",
        "${Environment.getExternalStorageDirectory()}/roms",
        "${Environment.getExternalStorageDirectory()}/Games",
        "${Environment.getExternalStorageDirectory()}/ROMs",
        "${Environment.getExternalStorageDirectory()}/Emuladores",
        "${Environment.getExternalStorageDirectory()}/RetroArch/roms"
    )

    private val imageExtensions = listOf("png", "jpg", "jpeg", "webp")

    fun scanRoms(): List<RomFile> {
        val roms = mutableListOf<RomFile>()

        // Obtener carpetas personalizadas
        val customPaths = getCustomPaths()
        val allPaths = defaultSearchPaths + customPaths

        for (path in allPaths) {
            val dir = File(path)
            if (dir.exists() && dir.isDirectory) {
                scanDirectory(dir, roms)
            }
        }

        // Eliminar duplicados - usar mapa para quedarse solo con el archivo mas grande
        val uniqueRomsMap = mutableMapOf<String, RomFile>()

        for (rom in roms) {
            // Normalizar nombre para detectar duplicados
            // Tomar solo las primeras 15 letras/números (sin espacios ni caracteres especiales)
            val normalizedName = rom.name
                .lowercase()
                .replace(Regex("[^a-z0-9]"), "") // Eliminar todo excepto letras y números
                .take(20) // Tomar solo los primeros 15 caracteres

            val key = "$normalizedName-${rom.platform.name}"

            // Si ya existe una ROM con el mismo nombre y plataforma, comparar tamaños
            val existing = uniqueRomsMap[key]
            if (existing == null) {
                // No existe, agregarlo
                uniqueRomsMap[key] = rom
            } else {
                // Ya existe, quedarse con el archivo de mayor tamaño
                val existingSize = File(existing.path).length()
                val newSize = File(rom.path).length()
                if (newSize > existingSize) {
                    uniqueRomsMap[key] = rom
                }
            }
        }

        return uniqueRomsMap.values.sortedBy { it.name }
    }

    private fun getCustomPaths(): List<String> {
        val prefs = context.getSharedPreferences("custom_paths", Context.MODE_PRIVATE)
        return prefs.getStringSet("paths", emptySet())?.toList() ?: emptyList()
    }

    private fun scanDirectory(directory: File, roms: MutableList<RomFile>) {
        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                scanDirectory(file, roms)
            } else {
                val extension = file.extension.lowercase()

                // Usar el método fromFile para mejor detección de plataforma
                val platform = Platform.fromFile(
                    fileName = file.name,
                    extension = extension,
                    filePath = file.absolutePath
                )

                if (platform != Platform.UNKNOWN) {
                    val coverPath = findCoverImage(file)
                    val romName = file.nameWithoutExtension
                        .replace("_", " ")
                        .replace("-", " ")
                        .replace("[", "")
                        .replace("]", "")
                        .replace("(", "")
                        .replace(")", "")
                        .trim()

                    roms.add(
                        RomFile(
                            name = romName,
                            path = file.absolutePath,
                            coverPath = coverPath,
                            platform = platform
                        )
                    )
                }
            }
        }
    }

    private fun findCoverImage(romFile: File): String? {
        val baseName = romFile.nameWithoutExtension
        val directory = romFile.parentFile ?: return null

        // Buscar en el mismo directorio
        for (ext in imageExtensions) {
            val coverFile = File(directory, "$baseName.$ext")
            if (coverFile.exists()) {
                return coverFile.absolutePath
            }
        }

        // Buscar en subcarpeta "covers"
        val coversDir = File(directory, "covers")
        if (coversDir.exists()) {
            for (ext in imageExtensions) {
                val coverFile = File(coversDir, "$baseName.$ext")
                if (coverFile.exists()) {
                    return coverFile.absolutePath
                }
            }
        }

        return null
    }
}

