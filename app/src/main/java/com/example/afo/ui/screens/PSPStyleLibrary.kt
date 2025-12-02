package com.example.afo.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afo.models.RomFile
import com.example.afo.ui.theme.*
import java.io.File

@Composable
fun PSPStyleLibrary(
    roms: List<RomFile>,
    onRomClick: (RomFile) -> Unit,
    onFavoriteToggle: (RomFile) -> Unit,
    onRefresh: () -> Unit = {}
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    var isUsingGamepad by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }

    // Solicitar foco al iniciar y cuando cambian las ROMs
    LaunchedEffect(roms.size) {
        kotlinx.coroutines.delay(100) // Pequeño delay para asegurar que el composable esté listo
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {
            // Ignorar si falla
        }
    }

    // Auto-scroll al item seleccionado SOLO cuando se usa mando/teclado
    LaunchedEffect(selectedIndex, isUsingGamepad) {
        if (roms.isNotEmpty() && isUsingGamepad) {
            listState.animateScrollToItem(
                index = selectedIndex.coerceIn(0, roms.lastIndex),
                scrollOffset = -200
            )
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0E27),
                        Color(0xFF1A1F3A),
                        Color(0xFF0A0E27)
                    )
                )
            )
            .padding(bottom = 24.dp)
            .focusRequester(focusRequester)
            .focusTarget()
            .onKeyEvent { keyEvent ->
                if (roms.isEmpty()) return@onKeyEvent false

                when {
                    // Tecla izquierda del teclado o mando
                    keyEvent.type == KeyEventType.KeyDown &&
                    (keyEvent.key == Key.DirectionLeft || keyEvent.key == Key.A) -> {
                        isUsingGamepad = true
                        if (selectedIndex > 0) {
                            selectedIndex--
                        }
                        true
                    }
                    // Tecla derecha del teclado o mando
                    keyEvent.type == KeyEventType.KeyDown &&
                    (keyEvent.key == Key.DirectionRight || keyEvent.key == Key.D) -> {
                        isUsingGamepad = true
                        if (selectedIndex < roms.lastIndex) {
                            selectedIndex++
                        }
                        true
                    }
                    // Enter del teclado o botón central del mando
                    keyEvent.type == KeyEventType.KeyDown &&
                    (keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter || keyEvent.key == Key.Spacebar) -> {
                        isUsingGamepad = true
                        onRomClick(roms[selectedIndex])
                        true
                    }
                    // Botón A del gamepad
                    keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.ButtonA -> {
                        isUsingGamepad = true
                        onRomClick(roms[selectedIndex])
                        true
                    }
                    // Botón Y/Triángulo del gamepad para agregar/quitar favoritos
                    keyEvent.type == KeyEventType.KeyDown && (keyEvent.key == Key.ButtonY || keyEvent.key == Key.DirectionUp) -> {
                        isUsingGamepad = true
                        if (selectedIndex in roms.indices) {
                            onFavoriteToggle(roms[selectedIndex])
                        }
                        true
                    }
                    else -> false
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header info del juego seleccionado
            if (roms.isNotEmpty() && selectedIndex < roms.size) {
                PSPGameHeader(
                    rom = roms[selectedIndex],
                    onFavoriteToggle = { onFavoriteToggle(roms[selectedIndex]) },
                    onRefresh = onRefresh
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Carrusel de juegos estilo PSP - scroll libre sin snap
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 100.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(roms) { index, _ ->
                    PSPGameCard(
                        rom = roms[index],
                        isSelected = index == selectedIndex && isUsingGamepad,
                        onClick = {
                            isUsingGamepad = false
                            selectedIndex = index
                            onRomClick(roms[index])
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(56.dp))

            // Info adicional
            if (roms.isNotEmpty() && selectedIndex < roms.size) {
                PSPGameInfo(rom = roms[selectedIndex])
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PSPGameHeader(
    rom: RomFile,
    onFavoriteToggle: () -> Unit,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        // Background con blur de la portada
        if (rom.coverPath != null && File(rom.coverPath).exists()) {
            val bitmap = remember(rom.coverPath) {
                BitmapFactory.decodeFile(rom.coverPath)
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(50.dp),
                    contentScale = ContentScale.Crop,
                    alpha = 0.3f
                )
            }
        }

        // Gradiente overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF0A0E27).copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // Contenido
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Badge de plataforma
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = rom.platform.color.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = rom.platform.displayName,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Nombre del juego
                    Text(
                        text = rom.name,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Botones de acción
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón de refrescar
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refrescar",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Botón de favorito
                    IconButton(onClick = onFavoriteToggle) {
                        Icon(
                            imageVector = if (rom.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (rom.isFavorite) FavoriteGold else TextSecondary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PSPGameCard(
    rom: RomFile,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Solo aplicar escala si está seleccionado (modo gamepad)
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 20.dp else 8.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "cardElevation"
    )

    Card(
        modifier = Modifier
            .width(180.dp)
            .height(250.dp)
            .scale(scale)
            .shadow(elevation, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CardBackground else DarkSurfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Portada
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

                    // Gradiente inferior
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
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
                    GamePlaceholder(rom)
                }
            } else {
                GamePlaceholder(rom)
            }

            // Nombre del juego
            if (isSelected) {
                Text(
                    text = rom.name,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Indicador de selección
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .align(Alignment.BottomCenter)
                        .background(PrimaryBlue)
                )
            }
        }
    }
}

@Composable
private fun PSPGameInfo(rom: RomFile) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Divider(
            color = TextTertiary.copy(alpha = 0.3f),
            thickness = 1.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            InfoItem(
                icon = Icons.Filled.VideogameAsset,
                label = "Plataforma",
                value = rom.platform.displayName
            )

            InfoItem(
                icon = Icons.Filled.Folder,
                label = "Archivo",
                value = File(rom.path).extension.uppercase()
            )

            val fileSize = File(rom.path).length()
            val fileSizeStr = when {
                fileSize > 1024 * 1024 * 1024 -> "%.1f GB".format(fileSize / (1024.0 * 1024.0 * 1024.0))
                fileSize > 1024 * 1024 -> "%.0f MB".format(fileSize / (1024.0 * 1024.0))
                else -> "%.0f KB".format(fileSize / 1024.0)
            }
            InfoItem(
                icon = Icons.Filled.InsertDriveFile,
                label = "Tamaño",
                value = fileSizeStr
            )
        }
    }
}

@Composable
private fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = PrimaryBlue,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GamePlaceholder(rom: RomFile) {
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
                imageVector = Icons.Filled.VideogameAsset,
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

