package com.example.afo.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afo.FavoritesManager
import com.example.afo.FolderPickerManager
import com.example.afo.RomScanner
import com.example.afo.models.RomFile
import com.example.afo.ui.theme.*
import java.io.File

@Composable
fun RomGridScreen(
    roms: List<RomFile> = emptyList(),
    isLoading: Boolean = false,
    onRomClick: (RomFile) -> Unit,
    onRefresh: () -> Unit = {},
    initialShowFavoritesOnly: Boolean = false
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showFavoritesOnly by remember { mutableStateOf(initialShowFavoritesOnly) }
    var showFoldersDialog by remember { mutableStateOf(false) }
    var usePSPStyle by remember { mutableStateOf(true) } // Vista PSP por defecto
    val favoritesManager = remember { FavoritesManager(context) }

    // Aplicar estado de favoritos a las ROMs recibidas
    var romsWithFavorites by remember { mutableStateOf<List<RomFile>>(emptyList()) }

    LaunchedEffect(roms) {
        romsWithFavorites = roms.map { rom ->
            rom.copy(isFavorite = favoritesManager.isFavorite(rom.path))
        }
    }

    val filteredRoms = remember(romsWithFavorites, searchQuery, showFavoritesOnly) {
        romsWithFavorites.filter { rom ->
            val matchesSearch = rom.name.contains(searchQuery, ignoreCase = true) ||
                    rom.platform.displayName.contains(searchQuery, ignoreCase = true)
            val matchesFavorite = !showFavoritesOnly || rom.isFavorite
            matchesSearch && matchesFavorite
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // Barra de búsqueda y filtros (solo si no estamos en modo PSP sin filtros)
        if (!usePSPStyle || searchQuery.isNotEmpty() || showFavoritesOnly) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Buscar ROMs..."
                )

                Spacer(modifier = Modifier.height(8.dp))

            // Filtros y opciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = showFavoritesOnly,
                        onClick = { showFavoritesOnly = !showFavoritesOnly },
                        label = {
                            Text(
                                text = "Favoritos",
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (showFavoritesOnly) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DarkSurfaceVariant,
                            selectedContainerColor = FavoriteGold.copy(alpha = 0.3f),
                            labelColor = TextSecondary,
                            selectedLabelColor = FavoriteGold,
                            iconColor = TextSecondary,
                            selectedLeadingIconColor = FavoriteGold
                        )
                    )

                    // Botón de cambio de vista
                    IconButton(
                        onClick = { usePSPStyle = !usePSPStyle }
                    ) {
                        Icon(
                            imageVector = if (usePSPStyle) Icons.Filled.Apps else Icons.Filled.ViewAgenda,
                            contentDescription = if (usePSPStyle) "Vista Grid" else "Vista PSP",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Text(
                    text = "${filteredRoms.size} juegos",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        }

        // Lista de ROMs
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "romLoading",
            modifier = Modifier.weight(1f)
        ) { loading ->
            if (loading) {
                LoadingIndicator(text = "Escaneando ROMs...")
            } else {
                if (filteredRoms.isEmpty()) {
                    EmptyState(
                        icon = if (showFavoritesOnly) Icons.Filled.FavoriteBorder else Icons.Filled.Star,
                        title = if (showFavoritesOnly) "Sin favoritos" else "Sin ROMs",
                        message = if (showFavoritesOnly)
                            "No has marcado ningún juego como favorito aún."
                        else
                            "No se encontraron ROMs.\n\nCarpetas predeterminadas:\n• /Download/roms\n• /roms\n• /Games\n\nUsa el botón + para agregar carpetas personalizadas.",
                        actionButton = if (!showFavoritesOnly) {
                            {
                                AnimatedButton(
                                    onClick = onRefresh,
                                    text = "Refrescar",
                                    icon = Icons.Filled.Refresh
                                )
                            }
                        } else null
                    )
                } else {
                    // Vista PSP o Grid según preferencia
                    if (usePSPStyle && searchQuery.isEmpty() && !showFavoritesOnly) {
                        PSPStyleLibrary(
                            roms = filteredRoms,
                            onRomClick = onRomClick,
                            onFavoriteToggle = { rom ->
                                val newState = favoritesManager.toggleFavorite(rom.path)
                                romsWithFavorites = romsWithFavorites.map {
                                    if (it.path == rom.path) it.copy(isFavorite = newState)
                                    else it
                                }
                            },
                            onRefresh = onRefresh
                        )
                    } else {
                        RomGrid(
                            roms = filteredRoms,
                            onRomClick = onRomClick,
                            onFavoriteToggle = { rom ->
                                val newState = favoritesManager.toggleFavorite(rom.path)
                                romsWithFavorites = romsWithFavorites.map {
                                    if (it.path == rom.path) it.copy(isFavorite = newState)
                                    else it
                                }
                            }
                        )
                    }
                }
            }
        }
        }

        // Botones flotantes
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botón para ver carpetas
            SmallFloatingActionButton(
                onClick = { showFoldersDialog = true },
                containerColor = DarkSurfaceVariant,
                contentColor = TextPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.FolderOpen,
                    contentDescription = "Ver carpetas"
                )
            }

            // Botón para agregar carpetas
            FloatingActionButton(
                onClick = {
                    FolderPickerManager.onRefreshCallback = onRefresh
                    val activity = context as? android.app.Activity
                    activity?.let {
                        try {
                            val openPickerMethod = activity.javaClass.getMethod("openFolderPicker")
                            openPickerMethod.invoke(activity)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                containerColor = PrimaryBlue,
                contentColor = TextPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Agregar carpeta"
                )
            }
        }

        // Diálogo de gestión de carpetas
        if (showFoldersDialog) {
            FoldersManagementDialog(
                onDismiss = { showFoldersDialog = false },
                onRefresh = onRefresh
            )
        }
    }
}

@Composable
private fun RomGrid(
    roms: List<RomFile>,
    onRomClick: (RomFile) -> Unit,
    onFavoriteToggle: (RomFile) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(
            items = roms,
            key = { it.path }
        ) { rom ->
            RomCard(
                rom = rom,
                onClick = { onRomClick(rom) },
                onFavoriteToggle = { onFavoriteToggle(rom) }
            )
        }
    }
}

@Composable
private fun RomCard(
    rom: RomFile,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.92f
            isHovered -> 1.08f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "romCardScale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isHovered) 20.dp else 8.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "romCardElevation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .scale(scale)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .shadow(elevation, RoundedCornerShape(20.dp))
                .clickable {
                    isPressed = true
                    onClick()
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Imagen de portada o placeholder
                if (rom.coverPath != null && File(rom.coverPath).exists()) {
                    val bitmap = remember(rom.coverPath) {
                        BitmapFactory.decodeFile(rom.coverPath)
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = rom.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Gradiente en la parte inferior para mejorar legibilidad
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.9f)
                                        )
                                    )
                                )
                        )
                    } else {
                        RomPlaceholder(rom)
                    }
                } else {
                    RomPlaceholder(rom)
                }

                // Botón de favorito (esquina superior derecha)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(40.dp)
                        .shadow(4.dp, RoundedCornerShape(20.dp))
                        .background(
                            color = OverlayDark,
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    FavoriteButton(
                        isFavorite = rom.isFavorite,
                        onToggle = onFavoriteToggle,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Información en la parte inferior
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // Badge de plataforma
                    PlatformIcon(
                        platform = rom.platform,
                        size = 36
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Nombre del juego
                    Text(
                        text = rom.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun RomPlaceholder(rom: RomFile) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        rom.platform.color.copy(alpha = 0.6f),
                        rom.platform.color.copy(alpha = 0.3f),
                        CardBackground
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = TextTertiary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = rom.platform.displayName,
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun FoldersManagementDialog(
    onDismiss: () -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("custom_paths", android.content.Context.MODE_PRIVATE)
    var customPaths by remember { mutableStateOf(prefs.getStringSet("paths", emptySet())?.toList() ?: emptyList()) }

    // Carpetas predeterminadas
    val defaultPaths = listOf(
        "/storage/emulated/0/Download/roms",
        "/storage/emulated/0/roms",
        "/storage/emulated/0/Games",
        "/storage/emulated/0/ROMs",
        "/storage/emulated/0/Emuladores",
        "/storage/emulated/0/RetroArch/roms"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.FolderOpen,
                    contentDescription = null,
                    tint = PrimaryBlue
                )
                Text("Carpetas de ROMs")
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Carpetas predeterminadas
                item {
                    Text(
                        text = "Carpetas predeterminadas:",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                }

                items(defaultPaths) { path ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Folder,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = path,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Carpetas personalizadas
                if (customPaths.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Carpetas personalizadas:",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                    }

                    items(customPaths.size) { index ->
                        val path = customPaths[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FolderSpecial,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = path,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    // Eliminar carpeta
                                    val updatedPaths = prefs.getStringSet("paths", emptySet())?.toMutableSet() ?: mutableSetOf()
                                    updatedPaths.remove(path)
                                    prefs.edit().putStringSet("paths", updatedPaths).apply()
                                    customPaths = updatedPaths.toList()
                                    onRefresh()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Eliminar",
                                    tint = ErrorRed
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total: ${customPaths.size} carpeta(s) personalizada(s)",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = PrimaryBlue)
            }
        },
        containerColor = DarkSurface,
        iconContentColor = PrimaryBlue,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}
