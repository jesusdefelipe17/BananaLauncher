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
import com.example.afo.models.RomFile
import com.example.afo.ui.theme.*
import java.io.File

@Composable
fun FavoritesScreen(
    roms: List<RomFile>,
    isLoading: Boolean = false,
    onRomClick: (RomFile) -> Unit,
    onRefresh: () -> Unit = {}
) {
    val context = LocalContext.current
    var showFoldersDialog by remember { mutableStateOf(false) }
    val favoritesManager = remember { FavoritesManager(context) }

    // Filtrar SOLO favoritos
    val filteredRoms by remember(roms) {
        derivedStateOf {
            roms.filter { rom ->
                favoritesManager.isFavorite(rom.path)
            }.map { rom ->
                rom.copy(isFavorite = true)
            }
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
                    LoadingIndicator(text = "Cargando favoritos...")
                } else {
                    if (filteredRoms.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                // Icono animado
                                val infiniteTransition = rememberInfiniteTransition(label = "heartBeat")
                                val heartScale by infiniteTransition.animateFloat(
                                    initialValue = 1f,
                                    targetValue = 1.2f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1000, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "heartScale"
                                )

                                Icon(
                                    imageVector = Icons.Filled.FavoriteBorder,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .scale(heartScale),
                                    tint = FavoriteGold.copy(alpha = 0.5f)
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                Text(
                                    text = "✨ Sin favoritos aún",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Empieza a marcar tus juegos favoritos",
                                    fontSize = 16.sp,
                                    color = TextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Card(
                                    modifier = Modifier.padding(horizontal = 32.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = FavoriteGold.copy(alpha = 0.1f)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Favorite,
                                            contentDescription = null,
                                            tint = FavoriteGold,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "Ve a la biblioteca y toca el corazón en cualquier juego",
                                            fontSize = 14.sp,
                                            color = TextPrimary,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                Button(
                                    onClick = onRefresh,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = FavoriteGold
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Refrescar",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    } else {
                        FavoriteRomGrid(
                            roms = filteredRoms,
                            onRomClick = onRomClick,
                            onFavoriteToggle = { rom ->
                                favoritesManager.toggleFavorite(rom.path)
                            }
                        )
                    }
                }
            }
        }

        // Botones flotantes con estilo de favoritos
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmallFloatingActionButton(
                onClick = { showFoldersDialog = true },
                containerColor = FavoriteGold.copy(alpha = 0.2f),
                contentColor = FavoriteGold
            ) {
                Icon(
                    imageVector = Icons.Filled.FolderOpen,
                    contentDescription = "Ver carpetas"
                )
            }

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
                containerColor = FavoriteGold,
                contentColor = Color.Black
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Agregar carpeta"
                )
            }
        }

        if (showFoldersDialog) {
            FavoritesFoldersDialog(
                onDismiss = { showFoldersDialog = false },
                onRefresh = onRefresh
            )
        }
    }
}

@Composable
private fun FavoriteRomGrid(
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
            FavoriteRomCard(
                rom = rom,
                onClick = { onRomClick(rom) },
                onFavoriteToggle = { onFavoriteToggle(rom) }
            )
        }
    }
}

@Composable
private fun FavoriteRomCard(
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
        // Borde dorado para favoritos
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            FavoriteGold.copy(alpha = 0.6f),
                            FavoriteGold.copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(2.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(elevation, RoundedCornerShape(18.dp))
                    .clickable {
                        isPressed = true
                        onClick()
                    },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                )
            ) {
            Box(modifier = Modifier.fillMaxSize()) {
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
                        FavoriteRomPlaceholder(rom)
                    }
                } else {
                    FavoriteRomPlaceholder(rom)
                }

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

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    PlatformIcon(
                        platform = rom.platform,
                        size = 36
                    )

                    Spacer(modifier = Modifier.height(8.dp))

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
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun FavoriteRomPlaceholder(rom: RomFile) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        FavoriteGold.copy(alpha = 0.3f),
                        rom.platform.color.copy(alpha = 0.4f),
                        CardBackground
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Fondo con brillo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            FavoriteGold.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        center = androidx.compose.ui.geometry.Offset(0.5f, 0.3f)
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono de estrella con brillo
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = FavoriteGold.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = FavoriteGold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = rom.platform.displayName,
                color = FavoriteGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FavoritesFoldersDialog(
    onDismiss: () -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("custom_paths", android.content.Context.MODE_PRIVATE)
    var customPaths by remember { mutableStateOf(prefs.getStringSet("paths", emptySet())?.toList() ?: emptyList()) }

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
                        text = "💡 Usa el botón + para agregar más carpetas",
                        color = TextTertiary,
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
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary
    )
}

