package com.example.afo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.afo.models.RomFile
import com.example.afo.ui.screens.*
import com.example.afo.ui.theme.*
import kotlinx.coroutines.*
import java.io.File

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (!allGranted) {
            Toast.makeText(
                this,
                "Se necesitan permisos de almacenamiento para escanear ROMs",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private val openFolderLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            try {
                // Tomar persistencia de permisos
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                // Obtener la ruta real desde el URI
                val path = getRealPathFromUri(uri)

                if (path != null) {
                    // Guardar en SharedPreferences
                    val prefs = getSharedPreferences("custom_paths", MODE_PRIVATE)
                    val existingPaths = prefs.getStringSet("paths", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

                    if (existingPaths.add(path)) {
                        prefs.edit().putStringSet("paths", existingPaths).apply()
                        Toast.makeText(this, "Carpeta agregada:\n$path", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Esta carpeta ya está agregada", Toast.LENGTH_SHORT).show()
                    }

                    // Callback para refrescar
                    FolderPickerManager.onRefreshCallback?.invoke()
                } else {
                    Toast.makeText(this, "No se pudo acceder a la carpeta", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error al agregar carpeta: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun getRealPathFromUri(uri: Uri): String? {
        return try {
            // Intentar obtener la ruta del URI
            val path = uri.path ?: return null

            // Extraer la parte después de los dos puntos
            val parts = path.split(":")
            if (parts.size >= 2) {
                val realPath = parts[1]
                // Construir la ruta completa
                "/storage/emulated/0/$realPath"
            } else {
                // Si no tiene el formato esperado, intentar usar el path directo
                if (path.startsWith("/tree/primary:")) {
                    "/storage/emulated/0/${path.substringAfter("/tree/primary:")}"
                } else if (path.startsWith("/tree/")) {
                    "/storage/emulated/0/${path.substringAfter("/tree/")}"
                } else {
                    path
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Solicitar permisos de almacenamiento
        checkAndRequestPermissions()

        setContent {
            AFOTheme {
                EmuLauncherApp()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+ (API 33+)
                val permissions = arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )

                val permissionsToRequest = permissions.filter {
                    ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
                }

                if (permissionsToRequest.isNotEmpty()) {
                    requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
                }

                // Pedir acceso completo al almacenamiento
                if (!Environment.isExternalStorageManager()) {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            data = Uri.parse("package:$packageName")
                        }
                        startActivity(intent)
                    } catch (_: Exception) {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        startActivity(intent)
                    }
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11-12 (API 30-32)
                if (!Environment.isExternalStorageManager()) {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            data = Uri.parse("package:$packageName")
                        }
                        startActivity(intent)
                    } catch (_: Exception) {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        startActivity(intent)
                    }
                }
            }
            else -> {
                // Android 10 y anteriores (API 29-)
                val permissions = arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )

                val permissionsToRequest = permissions.filter {
                    ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
                }

                if (permissionsToRequest.isNotEmpty()) {
                    requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
                }
            }
        }
    }

    fun openFolderPicker() {
        openFolderLauncher.launch(null)
    }

    fun getCustomPaths(): List<String> {
        val prefs = getSharedPreferences("custom_paths", MODE_PRIVATE)
        return prefs.getStringSet("paths", emptySet())?.toList() ?: emptyList()
    }

    fun clearCustomPaths() {
        val prefs = getSharedPreferences("custom_paths", MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}

enum class Screen {
    EMULATORS,
    ROMS,
    FAVORITES,
    SOCIAL,
    PROFILE,
    SETTINGS,
    ROM_DETAIL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmuLauncherApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(Screen.ROMS) } // Biblioteca es la inicial
    var selectedRom by remember { mutableStateOf<RomFile?>(null) }

    // Manejar el botón atrás
    androidx.activity.compose.BackHandler(enabled = currentScreen == Screen.ROM_DETAIL) {
        // Solo manejar el back cuando estamos en detalle de ROM
        currentScreen = Screen.ROMS
        selectedRom = null
    }

    // Para las pantallas principales (ROMS/EMULATORS), dejar que el sistema maneje el back (salir de la app)
    // No necesitamos hacer nada especial, el comportamiento por defecto ya lo hace

    // Estados para emuladores y ROMs
    var emulators by remember { mutableStateOf<List<com.example.afo.models.EmulatorApp>>(emptyList()) }
    var roms by remember { mutableStateOf<List<RomFile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Estados para el popup de selección de emulador
    var showEmulatorPicker by remember { mutableStateOf(false) }
    var availableEmulators by remember { mutableStateOf<List<com.example.afo.models.EmulatorApp>>(emptyList()) }
    var romToLaunch by remember { mutableStateOf<RomFile?>(null) }

    // Sistema de logros
    val achievementsManager = remember { com.example.afo.achievements.AchievementsManager.getInstance(context) }

    // Escanear emuladores y ROMs al inicio
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val appScanner = AppScanner(context)
            val romScanner = RomScanner(context)
            val coverDownloader = CoverDownloader(context)

            val scannedEmulators = appScanner.scanInstalledEmulators()
            var scannedRoms = romScanner.scanRoms()

            withContext(Dispatchers.Main) {
                emulators = scannedEmulators
                roms = scannedRoms
                isLoading = false
            }

            // Descargar carátulas en segundo plano
            scannedRoms = scannedRoms.map { rom ->
                if (rom.coverPath == null || !File(rom.coverPath).exists()) {
                    val downloadedCover = coverDownloader.downloadCover(rom.name, rom.platform.displayName)
                    rom.copy(coverPath = downloadedCover)
                } else {
                    rom
                }
            }

            // Actualizar ROMs con carátulas descargadas
            withContext(Dispatchers.Main) {
                roms = scannedRoms
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            if (currentScreen != Screen.ROM_DETAIL) {
                EmuLauncherTopBar(
                    currentScreen = currentScreen,
                    onTabSelected = { currentScreen = it }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    when (targetState) {
                        Screen.ROM_DETAIL -> {
                            slideInHorizontally(
                                animationSpec = tween(400),
                                initialOffsetX = { it }
                            ) + fadeIn(animationSpec = tween(400)) togetherWith
                                    slideOutHorizontally(
                                        animationSpec = tween(400),
                                        targetOffsetX = { -it / 4 }
                                    ) + fadeOut(animationSpec = tween(200))
                        }
                        else -> {
                            fadeIn(animationSpec = tween(300)) togetherWith
                                    fadeOut(animationSpec = tween(300))
                        }
                    }
                },
                label = "screenTransition"
            ) { screen ->
                when (screen) {
                    Screen.EMULATORS -> {
                        EmulatorListScreen(
                            emulators = emulators,
                            isLoading = isLoading,
                            onRefresh = {
                                isLoading = true
                                scope.launch(Dispatchers.IO) {
                                    val appScanner = AppScanner(context)
                                    val scannedEmulators = appScanner.scanInstalledEmulators()
                                    withContext(Dispatchers.Main) {
                                        emulators = scannedEmulators
                                        isLoading = false
                                    }
                                }
                            }
                        )
                    }
                    Screen.FAVORITES -> {
                        FavoritesScreen(
                            roms = roms,
                            isLoading = isLoading,
                            onRomClick = { rom: RomFile ->
                                selectedRom = rom
                                currentScreen = Screen.ROM_DETAIL
                            },
                            onRefresh = {
                                isLoading = true
                                scope.launch(Dispatchers.IO) {
                                    val romScanner = RomScanner(context)
                                    val coverDownloader = CoverDownloader(context)
                                    var scannedRoms = romScanner.scanRoms()

                                    withContext(Dispatchers.Main) {
                                        roms = scannedRoms
                                        isLoading = false
                                    }

                                    // Descargar carátulas en segundo plano
                                    scannedRoms = scannedRoms.map { rom ->
                                        if (rom.coverPath == null || !File(rom.coverPath).exists()) {
                                            val downloadedCover = coverDownloader.downloadCover(rom.name, rom.platform.displayName)
                                            rom.copy(coverPath = downloadedCover)
                                        } else {
                                            rom
                                        }
                                    }

                                    withContext(Dispatchers.Main) {
                                        roms = scannedRoms
                                    }
                                }
                            }
                        )
                    }
                    Screen.ROMS -> {
                        RomGridScreen(
                            roms = roms,
                            isLoading = isLoading,
                            onRomClick = { rom ->
                                selectedRom = rom
                                currentScreen = Screen.ROM_DETAIL
                            },
                            onRefresh = {
                                isLoading = true
                                scope.launch(Dispatchers.IO) {
                                    val romScanner = RomScanner(context)
                                    val coverDownloader = CoverDownloader(context)
                                    var scannedRoms = romScanner.scanRoms()

                                    withContext(Dispatchers.Main) {
                                        roms = scannedRoms
                                        isLoading = false
                                    }

                                    // Descargar carátulas en segundo plano
                                    scannedRoms = scannedRoms.map { rom ->
                                        if (rom.coverPath == null || !File(rom.coverPath).exists()) {
                                            val downloadedCover = coverDownloader.downloadCover(rom.name, rom.platform.displayName)
                                            rom.copy(coverPath = downloadedCover)
                                        } else {
                                            rom
                                        }
                                    }

                                    withContext(Dispatchers.Main) {
                                        roms = scannedRoms
                                    }
                                }
                            }
                        )
                    }
                    Screen.SOCIAL -> {
                        SocialScreen()
                    }
                    Screen.PROFILE -> {
                        ProfileScreen()
                    }
                    Screen.SETTINGS -> {
                        SettingsScreen()
                    }
                    Screen.ROM_DETAIL -> {
                        selectedRom?.let { rom ->

                            RomDetailScreen(
                                rom = rom,
                                onBack = {
                                    currentScreen = Screen.ROMS
                                    selectedRom = null
                                },
                                onPlay = { playRom ->
                                    // Guardar la ROM que queremos lanzar
                                    romToLaunch = playRom

                                    // Buscar todos los emuladores compatibles con la plataforma
                                    availableEmulators = emulators.filter {
                                        it.supportedPlatforms.contains(playRom.platform)
                                    }

                                    android.util.Log.d("EmuLauncher", "=== LANZANDO ROM ===")
                                    android.util.Log.d("EmuLauncher", "ROM: ${playRom.name}")
                                    android.util.Log.d("EmuLauncher", "Plataforma: ${playRom.platform.displayName}")
                                    android.util.Log.d("EmuLauncher", "Total emuladores instalados: ${emulators.size}")
                                    emulators.forEach {
                                        android.util.Log.d("EmuLauncher", "  - ${it.name} (${it.packageName}): ${it.supportedPlatforms.map { p -> p.displayName }}")
                                    }
                                    android.util.Log.d("EmuLauncher", "Emuladores compatibles encontrados: ${availableEmulators.size}")
                                    availableEmulators.forEach {
                                        android.util.Log.d("EmuLauncher", "  - ${it.name} (${it.packageName})")
                                    }

                                    when {
                                        availableEmulators.isEmpty() -> {
                                            android.util.Log.w("EmuLauncher", "No hay emuladores disponibles")
                                            Toast.makeText(
                                                context,
                                                "No hay emulador instalado para ${playRom.platform.displayName}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        availableEmulators.size == 1 -> {
                                            // Solo hay un emulador, lanzarlo directamente
                                            android.util.Log.d("EmuLauncher", "Solo un emulador disponible, lanzando directamente...")
                                            val appScanner = AppScanner(context)
                                            val success = appScanner.launchEmulatorWithRom(
                                                availableEmulators.first().packageName,
                                                playRom.path
                                            )
                                            if (success) {
                                                // Registrar en logros/estadísticas
                                                achievementsManager.recordGameOpened(
                                                    playRom.name,
                                                    playRom.platform.displayName
                                                )
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "No se pudo abrir el emulador. Verifica que esté instalado.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                        else -> {
                                            // Múltiples emuladores disponibles, mostrar selector
                                            android.util.Log.d("EmuLauncher", "Múltiples emuladores disponibles, mostrando selector...")
                                            showEmulatorPicker = true
                                        }
                                    }
                                }
                            )

                        }
                    }
                }
            }

            // Diálogo de selección de emulador (fuera del AnimatedContent)
            if (showEmulatorPicker && romToLaunch != null) {
                EmulatorPickerDialog(
                    rom = romToLaunch!!,
                    emulators = availableEmulators,
                    onDismiss = {
                        showEmulatorPicker = false
                        android.util.Log.d("EmuLauncher", "Diálogo cerrado sin selección")
                    },
                    onEmulatorSelected = { selectedEmulator ->
                        android.util.Log.d("EmuLauncher", "Emulador seleccionado: ${selectedEmulator.name}")
                        showEmulatorPicker = false
                        val appScanner = AppScanner(context)
                        val success = appScanner.launchEmulatorWithRom(
                            selectedEmulator.packageName,
                            romToLaunch!!.path
                        )
                        if (success) {
                            // Registrar en logros/estadísticas
                            achievementsManager.recordGameOpened(
                                romToLaunch!!.name,
                                romToLaunch!!.platform.displayName
                            )
                        } else {
                            Toast.makeText(
                                context,
                                "No se pudo abrir ${selectedEmulator.name}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmuLauncherTopBar(
    currentScreen: Screen,
    onTabSelected: (Screen) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
    ) {
        // Header
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Banana Launcher",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "Tu biblioteca de juegos",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = DarkSurface
            )
        )

        // Tabs
        TabRow(
            selectedTabIndex = when (currentScreen) {
                Screen.ROMS -> 0
                Screen.FAVORITES -> 1
                Screen.PROFILE -> 2
                Screen.SETTINGS -> 3
                Screen.EMULATORS -> 4
                Screen.SOCIAL -> 5
                else -> 0
            },
            containerColor = DarkSurface,
            contentColor = TextPrimary
        ) {
            Tab(
                selected = currentScreen == Screen.ROMS,
                onClick = { onTabSelected(Screen.ROMS) },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Biblioteca",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                },
                selectedContentColor = PrimaryBlue,
                unselectedContentColor = TextSecondary
            )

            Tab(
                selected = currentScreen == Screen.FAVORITES,
                onClick = { onTabSelected(Screen.FAVORITES) },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Favoritos",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                },
                selectedContentColor = PrimaryBlue,
                unselectedContentColor = TextSecondary
            )

            Tab(
                selected = currentScreen == Screen.PROFILE,
                onClick = { onTabSelected(Screen.PROFILE) },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Perfil",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                },
                selectedContentColor = PrimaryBlue,
                unselectedContentColor = TextSecondary
            )

            Tab(
                selected = currentScreen == Screen.SETTINGS,
                onClick = { onTabSelected(Screen.SETTINGS) },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Palette,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Temas",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                },
                selectedContentColor = PrimaryBlue,
                unselectedContentColor = TextSecondary
            )

            Tab(
                selected = currentScreen == Screen.SOCIAL,
                onClick = { onTabSelected(Screen.SOCIAL) },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.People,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Social",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                },
                selectedContentColor = PrimaryBlue,
                unselectedContentColor = TextSecondary
            )

            Tab(
                selected = currentScreen == Screen.EMULATORS,
                onClick = { onTabSelected(Screen.EMULATORS) },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Emuladores",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                },
                selectedContentColor = PrimaryBlue,
                unselectedContentColor = TextSecondary
            )
        }
    }
}