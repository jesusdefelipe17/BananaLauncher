package com.example.afo.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afo.AppScanner
import com.example.afo.models.EmulatorApp
import com.example.afo.ui.theme.*

@Composable
fun EmulatorListScreen(
    emulators: List<EmulatorApp> = emptyList(),
    isLoading: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "emulatorLoading",
            modifier = Modifier.weight(1f)
        ) { loading ->
            if (loading) {
                LoadingIndicator(text = "Escaneando emuladores...")
            } else {
                if (emulators.isEmpty()) {
                    EmptyState(
                        icon = Icons.Filled.Warning,
                        title = "Sin emuladores",
                        message = "No se detectaron emuladores instalados.\n\n" +
                                "Emuladores compatibles:\n" +
                                "• PPSSPP\n" +
                                "• Yuzu\n" +
                                "• RPCS3\n\n" +
                                "Instala alguno y presiona refrescar.",
                        actionButton = {
                            AnimatedButton(
                                onClick = onRefresh,
                                text = "Refrescar",
                                icon = Icons.Filled.Refresh
                            )
                        }
                    )
                } else {
                    EmulatorList(
                        emulators = emulators,
                        context = context
                    )
                }
            }
        }
    }
}

@Composable
private fun EmulatorList(
    emulators: List<EmulatorApp>,
    context: Context
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(emulators) { emulator ->
            EmulatorCard(
                emulator = emulator,
                onClick = {
                    AppScanner(context).launchEmulator(emulator.packageName)
                }
            )
        }
    }
}

@Composable
private fun EmulatorCard(
    emulator: EmulatorApp,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var isPressed by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.92f
            isHovered -> 1.05f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "emulatorCardScale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isHovered) 16.dp else 6.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "emulatorCardElevation"
    )

    // Obtener el icono de la aplicación
    val appIcon = remember(emulator.packageName) {
        try {
            context.packageManager.getApplicationIcon(emulator.packageName)
        } catch (e: Exception) {
            null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .shadow(elevation, RoundedCornerShape(24.dp))
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icono del emulador
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(8.dp, RoundedCornerShape(20.dp))
                        .background(
                            color = androidx.compose.ui.graphics.Color.White,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (appIcon != null) {
                        // Convertir Drawable a Bitmap y luego a ImageBitmap
                        val bitmap = remember(appIcon) {
                            val width = appIcon.intrinsicWidth
                            val height = appIcon.intrinsicHeight
                            val bmp = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                            val canvas = android.graphics.Canvas(bmp)
                            appIcon.setBounds(0, 0, width, height)
                            appIcon.draw(canvas)
                            bmp
                        }

                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = emulator.name,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nombre del emulador
                Text(
                    text = emulator.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Plataformas soportadas
                if (emulator.supportedPlatforms.isNotEmpty()) {
                    Text(
                        text = emulator.supportedPlatforms.joinToString(", ") { it.displayName },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
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

