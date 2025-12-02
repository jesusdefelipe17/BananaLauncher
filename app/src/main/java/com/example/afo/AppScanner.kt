package com.example.afo

import android.content.Context
import android.content.pm.PackageManager
import com.example.afo.models.EmulatorApp
import com.example.afo.models.Platform

class AppScanner(private val context: Context) {

    // Lista de emuladores conocidos con sus package names
    private val knownEmulators = listOf(

        // PPSSPP - PSP
        EmulatorApp(
            packageName = "org.ppsspp.ppsspp",
            name = "PPSSPP",
            supportedPlatforms = listOf(Platform.PSP)
        ),
        EmulatorApp(
            packageName = "org.ppsspp.ppssppgold",
            name = "PPSSPP Gold",
            supportedPlatforms = listOf(Platform.PSP)
        ),
        EmulatorApp(
            packageName = "org.ppsspp.ppsspp.dev",
            name = "PPSSPP Dev",
            supportedPlatforms = listOf(Platform.PSP)
        )
    )

    fun scanInstalledEmulators(): List<EmulatorApp> {
        val installedEmulators = mutableListOf<EmulatorApp>()
        val packageManager = context.packageManager

        // Primero intentar con los emuladores conocidos
        for (emulator in knownEmulators) {
            try {
                packageManager.getPackageInfo(emulator.packageName, 0)
                installedEmulators.add(emulator)
            } catch (_: PackageManager.NameNotFoundException) {
                // Emulador no instalado
            }
        }

        // Buscar emuladores adicionales por nombre de app
        try {
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            android.util.Log.d("AppScanner", "Escaneando ${installedApps.size} aplicaciones instaladas...")

            for (appInfo in installedApps) {
                try {
                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    val packageName = appInfo.packageName
                    val appNameLower = appName.lowercase()
                    val packageNameLower = packageName.lowercase()

                    // Verificar si ya está en la lista
                    if (installedEmulators.any { it.packageName == packageName }) {
                        continue
                    }

                    // Excluir aplicaciones del sistema y paquetes no deseados
                    val excludedKeywords = listOf(
                        "android.providers",
                        "com.android",
                        "com.google.android",
                        "credencial",
                        "credential",
                        "gestor",
                        "manager",
                        "settings",
                        "system"
                    )

                    // Si el paquete o nombre contiene palabras excluidas del sistema, saltar
                    val isSystemApp = excludedKeywords.any { excluded ->
                        (packageNameLower.startsWith("com.android") || packageNameLower.startsWith("com.google.android")) &&
                        (appNameLower.contains(excluded) || packageNameLower.contains(excluded))
                    }

                    if (isSystemApp) {
                        continue
                    }

                // Buscar por palabras clave en el nombre o paquete
                val emulatorKeywords = listOf(
                    "ppsspp", "yuzu", "rpcs3", "rpcsx", "eden", "skyline", "egg ns"
                )

                    val isEmulator = emulatorKeywords.any { keyword ->
                        appNameLower.contains(keyword) || packageNameLower.contains(keyword)
                    }

                    if (isEmulator) {
                        // Detectar plataforma soportada por el nombre
                        val platforms = detectPlatformsByName(appNameLower, packageNameLower)

                        // Log para debug
                        android.util.Log.d("AppScanner", "Emulador detectado: $appName ($packageName) - Plataformas: ${platforms.map { it.displayName }}")

                        if (platforms.isNotEmpty()) {
                            installedEmulators.add(
                                EmulatorApp(
                                    packageName = packageName,
                                    name = appName,
                                    supportedPlatforms = platforms
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Ignorar apps problemáticas
                    continue
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AppScanner", "Error escaneando apps: ${e.message}", e)
        }

        android.util.Log.d("AppScanner", "Total emuladores encontrados: ${installedEmulators.size}")
        return installedEmulators.distinctBy { it.packageName }
    }

    private fun detectPlatformsByName(appName: String, packageName: String): List<Platform> {
        val name = "$appName $packageName".lowercase()
        val platforms = mutableListOf<Platform>()

        when {
            // PPSSPP - PSP
            name.contains("ppsspp") -> platforms.add(Platform.PSP)

            // Eden - Nintendo Switch
            name.contains("eden") -> platforms.add(Platform.SWITCH)

            // Yuzu - Nintendo Switch
            name.contains("yuzu") -> platforms.add(Platform.SWITCH)

            // RPCS3/RPCSX - PS3
            name.contains("rpcs3") || name.contains("rpcsx") -> {
                // Como no hay plataforma PS3, usamos PSX o agregamos una
                platforms.add(Platform.PSX)
            }
        }

        return platforms
    }

    fun launchEmulator(packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun launchEmulatorWithRom(packageName: String, romPath: String): Boolean {
        return try {
            val file = java.io.File(romPath)
            if (!file.exists()) {
                android.util.Log.e("AppScanner", "ROM no existe: $romPath")
                return false
            }

            android.util.Log.d("AppScanner", "Intentando lanzar: $packageName con ROM: $romPath")

            // Eden (Nintendo Switch)
            if (packageName.contains("eden", ignoreCase = true) || packageName.contains("chimerapps", ignoreCase = true)) {
                android.util.Log.d("AppScanner", "Detectado Eden - usando método FileProvider")

                try {
                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )

                    context.grantUriPermission(
                        packageName,
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, getMimeType(romPath))
                        setPackage(packageName)
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    context.startActivity(intent)
                    android.util.Log.d("AppScanner", "✓ Eden abierto")
                    return true
                } catch (e: Exception) {
                    android.util.Log.w("AppScanner", "Eden falló, abriendo emulador: ${e.message}")
                    return launchEmulator(packageName)
                }
            }

            // Yuzu (Android) - Emulador de Nintendo Switch
            if (packageName.contains("yuzu", ignoreCase = true)) {
                android.util.Log.d("AppScanner", "Detectado Yuzu - probando múltiples métodos")

                // Método 1: Uri.fromFile directo (algunos emuladores lo prefieren)
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        setDataAndType(android.net.Uri.fromFile(file), "application/octet-stream")
                        setPackage(packageName)
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(intent)
                    android.util.Log.d("AppScanner", "✓ Yuzu Método 1 (Uri.fromFile) exitoso")
                    return true
                } catch (e: Exception) {
                    android.util.Log.w("AppScanner", "Yuzu Método 1 falló: ${e.message}")
                }

                // Método 2: FileProvider con application/octet-stream
                try {
                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )

                    context.grantUriPermission(
                        packageName,
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/octet-stream")
                        setPackage(packageName)
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(intent)
                    android.util.Log.d("AppScanner", "✓ Yuzu Método 2 (FileProvider octet-stream) exitoso")
                    return true
                } catch (e: Exception) {
                    android.util.Log.w("AppScanner", "Yuzu Método 2 falló: ${e.message}")
                }

                // Método 3: Intent con extras y path
                try {
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                    if (launchIntent != null) {
                        launchIntent.apply {
                            putExtra("game_path", file.absolutePath)
                            putExtra("nsp_path", file.absolutePath)
                            putExtra("xci_path", file.absolutePath)
                            putExtra(android.content.Intent.EXTRA_STREAM, android.net.Uri.fromFile(file))
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(launchIntent)
                        android.util.Log.d("AppScanner", "✓ Yuzu Método 3 (extras) exitoso")
                        return true
                    }
                } catch (e: Exception) {
                    android.util.Log.w("AppScanner", "Yuzu Método 3 falló: ${e.message}")
                }

                // Método 4: Solo abrir Yuzu (fallback)
                android.util.Log.w("AppScanner", "Todos los métodos de Yuzu fallaron, abriendo emulador solo")
                return launchEmulator(packageName)
            }

            // Método genérico para todos los demás emuladores (PPSSPP, etc)
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setPackage(packageName)
                setDataAndType(uri, getMimeType(romPath))
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Intentar lanzar con el ROM
            try {
                context.startActivity(intent)
                android.util.Log.d("AppScanner", "ROM lanzado exitosamente: $romPath")
                return true
            } catch (e: Exception) {
                android.util.Log.w("AppScanner", "No se pudo lanzar ROM, abriendo emulador: ${e.message}")
                // Si falla, abrir el emulador solo
                val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                    return true
                }
            }


            false
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("AppScanner", "Error lanzando emulador: ${e.message}")
            // Intentar abrir el emulador sin el ROM
            launchEmulator(packageName)
        }
    }

    private fun getMimeType(path: String): String {
        val extension = path.substringAfterLast('.', "").lowercase()
        return when (extension) {
            // Nintendo Switch
            "nsp", "nsz" -> "application/x-nintendo-switch-nsp"
            "xci", "xcz" -> "application/x-nintendo-switch-xci"

            // PSP/PSX/PS2
            "iso" -> "application/x-iso9660-image"
            "cso" -> "application/x-cso-image"
            "chd" -> "application/x-chd-image"
            "pbp" -> "application/x-pbp-image"

            // PSX
            "bin", "cue" -> "application/x-cd-image"
            "img" -> "application/x-cd-image"

            // Nintendo portables
            "gba" -> "application/x-gba-rom"
            "nds" -> "application/x-nintendo-ds-rom"
            "gb", "gbc" -> "application/x-gameboy-rom"

            // Nintendo consoles
            "nes" -> "application/x-nes-rom"
            "snes", "smc", "sfc" -> "application/x-snes-rom"
            "n64", "z64", "v64" -> "application/x-n64-rom"

            // Sega
            "md", "gen", "smd" -> "application/x-genesis-rom"

            // Compressed
            "zip", "7z" -> "application/zip"

            else -> "application/octet-stream"
        }
    }
}

