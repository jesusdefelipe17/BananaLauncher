package com.example.afo.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.afo.models.Platform
import com.example.afo.ui.theme.*

@Composable
fun AnimatedButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "buttonScale"
    )

    Button(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier
            .scale(scale)
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryBlue,
            contentColor = TextPrimary,
            disabledContainerColor = DarkSurfaceVariant,
            disabledContentColor = TextTertiary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp,
            disabledElevation = 0.dp
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
fun PlatformBadge(
    platform: Platform,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .background(
                color = platform.color.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = platform.displayName,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
fun PlatformIcon(
    platform: Platform,
    modifier: Modifier = Modifier,
    size: Int = 40
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .shadow(6.dp, CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        platform.color,
                        platform.color.copy(alpha = 0.7f)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (platform) {
                Platform.GBA -> "GBA"
                Platform.NES -> "NES"
                Platform.SNES -> "SNES"
                Platform.N64 -> "N64"
                Platform.NDS -> "NDS"
                Platform.PSX -> "PS1"
                Platform.PSP -> "PSP"
                Platform.PS2 -> "PS2"
                Platform.SWITCH -> "NSW"
                Platform.GB -> "GB"
                Platform.GBC -> "GBC"
                Platform.MD -> "MD"
                Platform.ARCADE -> "ARC"
                else -> "?"
            },
            color = Color.White,
            fontSize = (size / 3).sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "favoriteScale"
    )

    val color by animateColorAsState(
        targetValue = if (isFavorite) FavoriteGold else TextTertiary,
        animationSpec = tween(300),
        label = "favoriteColor"
    )

    IconButton(
        onClick = onToggle,
        modifier = modifier.scale(scale)
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            tint = color,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    text: String = "Cargando..."
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = PrimaryBlue,
            strokeWidth = 6.dp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = text,
            color = TextSecondary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionButton: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = TextTertiary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            color = TextSecondary,
            fontSize = 16.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (actionButton != null) {
            Spacer(modifier = Modifier.height(32.dp))
            actionButton()
        }
    }
}

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    val elevation by animateDpAsState(
        targetValue = if (isHovered) 12.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "cardElevation"
    )

    val cardModifier = modifier
        .shadow(elevation, RoundedCornerShape(20.dp))
        .clip(RoundedCornerShape(20.dp))
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    CardBackground,
                    CardBackground.copy(alpha = 0.8f)
                )
            )
        )
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
            } else Modifier
        )

    Box(modifier = cardModifier) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar..."
) {
    val interactionSource = remember { MutableInteractionSource() }

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = {
            Text(
                text = placeholder,
                color = TextTertiary
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = TextSecondary
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear",
                        tint = TextSecondary
                    )
                }
            }
        },
        interactionSource = interactionSource,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DarkSurfaceVariant,
            unfocusedContainerColor = DarkSurfaceVariant,
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = PrimaryBlue,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )
}

@Composable
fun EmulatorPickerDialog(
    rom: com.example.afo.models.RomFile,
    emulators: List<com.example.afo.models.EmulatorApp>,
    onDismiss: () -> Unit,
    onEmulatorSelected: (com.example.afo.models.EmulatorApp) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val packageManager = context.packageManager

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Selecciona un emulador",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Para: ${rom.name}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                emulators.forEach { emulator ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEmulatorSelected(emulator) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkSurfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Icono de la app
                            val appIconBitmap = remember(emulator.packageName) {
                                try {
                                    val appIcon = packageManager.getApplicationIcon(emulator.packageName)
                                    appIcon.toBitmap().asImageBitmap()
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            if (appIconBitmap != null) {
                                androidx.compose.foundation.Image(
                                    painter = BitmapPainter(appIconBitmap),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            } else {
                                // Fallback si no se puede obtener el icono
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(PrimaryBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.VideogameAsset,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Nombre del emulador
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = emulator.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = emulator.supportedPlatforms.joinToString(", ") { it.displayName },
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            // Flecha
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancelar",
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary
    )
}
