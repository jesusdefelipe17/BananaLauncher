package com.example.afo.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afo.FavoritesManager
import com.example.afo.models.RomFile
import com.example.afo.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RomDetailScreen(
    rom: RomFile,
    onBack: () -> Unit,
    onPlay: (RomFile) -> Unit
) {
    val context = LocalContext.current
    val favoritesManager = remember { FavoritesManager(context) }
    var isFavorite by remember { mutableStateOf(favoritesManager.isFavorite(rom.path)) }

    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
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
    ) {
        // Imagen de fondo difuminada
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
                        .blur(80.dp),
                    contentScale = ContentScale.Crop,
                    alpha = 0.2f
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar con botón atrás
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Contenido principal
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Carátula a la izquierda
                    Card(
                        modifier = Modifier
                            .width(300.dp)
                            .fillMaxHeight(0.7f)
                            .shadow(32.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
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
                                } else {
                                    CoverPlaceholder(rom)
                                }
                            } else {
                                CoverPlaceholder(rom)
                            }

                            // Badge de plataforma
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = rom.platform.color.copy(alpha = 0.9f),
                                shadowElevation = 8.dp
                            ) {
                                Text(
                                    text = rom.platform.displayName,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Información a la derecha
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Nombre del juego
                        Text(
                            text = rom.name,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            lineHeight = 48.sp
                        )

                        // Botón de favorito
                        OutlinedButton(
                            onClick = {
                                isFavorite = favoritesManager.toggleFavorite(rom.path)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isFavorite) FavoriteGold.copy(alpha = 0.2f) else Color.Transparent,
                                contentColor = if (isFavorite) FavoriteGold else TextSecondary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorito",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isFavorite) "En favoritos" else "Agregar a favoritos",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Información del archivo
                        DetailInfoCard(rom)

                        Spacer(modifier = Modifier.height(8.dp))

                        // Botón de jugar grande
                        Button(
                            onClick = { onPlay(rom) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 12.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Jugar",
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "JUGAR AHORA",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailInfoCard(rom: RomFile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Información del archivo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            HorizontalDivider(
                color = TextTertiary.copy(alpha = 0.3f),
                thickness = 1.dp
            )

            val file = File(rom.path)
            val fileSize = file.length()
            val fileSizeStr = when {
                fileSize > 1024 * 1024 * 1024 -> "%.2f GB".format(fileSize / (1024.0 * 1024.0 * 1024.0))
                fileSize > 1024 * 1024 -> "%.1f MB".format(fileSize / (1024.0 * 1024.0))
                else -> "%.0f KB".format(fileSize / 1024.0)
            }

            DetailInfoRow(
                icon = Icons.Filled.VideogameAsset,
                label = "Plataforma",
                value = rom.platform.displayName,
                color = rom.platform.color
            )

            DetailInfoRow(
                icon = Icons.Filled.Folder,
                label = "Formato",
                value = file.extension.uppercase(),
                color = PrimaryBlue
            )

            DetailInfoRow(
                icon = Icons.Filled.Storage,
                label = "Tamaño",
                value = fileSizeStr,
                color = SuccessGreen
            )

            DetailInfoRow(
                icon = Icons.Filled.FolderOpen,
                label = "Ubicación",
                value = file.parent?.substringAfterLast("/") ?: "Desconocida",
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun DetailInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.2f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                )
            }
            Text(
                text = label,
                fontSize = 14.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = value,
            fontSize = 15.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CoverPlaceholder(rom: RomFile) {
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
                modifier = Modifier.size(96.dp),
                tint = Color.White.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = rom.platform.displayName,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


