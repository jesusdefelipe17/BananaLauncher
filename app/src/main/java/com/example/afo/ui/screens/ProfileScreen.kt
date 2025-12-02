package com.example.afo.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afo.achievements.Achievement
import com.example.afo.achievements.AchievementsManager
import com.example.afo.achievements.GameStats
import com.example.afo.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class ProfileTab {
    ACHIEVEMENTS,
    STATS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val achievementsManager = remember { AchievementsManager.getInstance(context) }

    var selectedTab by remember { mutableStateOf(ProfileTab.ACHIEVEMENTS) }

    val achievements by achievementsManager.achievements.collectAsState()
    val gameStats by achievementsManager.gameStats.collectAsState()
    val totalPlayTime by achievementsManager.totalPlayTime.collectAsState()
    val gamesPlayed by achievementsManager.gamesPlayed.collectAsState()

    val unlockedPercentage = achievementsManager.getUnlockedPercentage()
    val unlockedCount = achievements.count { it.isUnlocked }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header con resumen
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DarkSurface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Mi Perfil",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tarjetas de resumen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = "🎮",
                            value = gamesPlayed.toString(),
                            label = "Juegos"
                        )

                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = "⏱️",
                            value = formatPlayTime(totalPlayTime),
                            label = "Tiempo"
                        )

                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = "🏆",
                            value = "$unlockedCount/${achievements.size}",
                            label = "Logros"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Barra de progreso de logros
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progreso de Logros",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            Text(
                                text = "${unlockedPercentage.toInt()}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { unlockedPercentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PrimaryBlue,
                            trackColor = DarkSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tabs
                    TabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = Color.Transparent,
                        contentColor = TextPrimary
                    ) {
                        Tab(
                            selected = selectedTab == ProfileTab.ACHIEVEMENTS,
                            onClick = { selectedTab = ProfileTab.ACHIEVEMENTS },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.EmojiEvents,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text("Logros")
                                }
                            },
                            selectedContentColor = PrimaryBlue,
                            unselectedContentColor = TextSecondary
                        )

                        Tab(
                            selected = selectedTab == ProfileTab.STATS,
                            onClick = { selectedTab = ProfileTab.STATS },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Insights,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text("Estadísticas")
                                }
                            },
                            selectedContentColor = PrimaryBlue,
                            unselectedContentColor = TextSecondary
                        )
                    }
                }
            }

            // Contenido según tab
            AnimatedContent(
                targetState = selectedTab,
                label = "profileTabs"
            ) { tab ->
                when (tab) {
                    ProfileTab.ACHIEVEMENTS -> {
                        AchievementsContent(achievements = achievements)
                    }
                    ProfileTab.STATS -> {
                        StatsContent(
                            gameStats = gameStats,
                            achievementsManager = achievementsManager
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: String,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun AchievementsContent(achievements: List<Achievement>) {
    if (achievements.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.EmojiEvents,
            title = "Sin logros aún",
            message = "Juega para desbloquear logros y coleccionar trofeos"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Logros desbloqueados
            val unlocked = achievements.filter { it.isUnlocked }
            val locked = achievements.filter { !it.isUnlocked }

            if (unlocked.isNotEmpty()) {
                item {
                    Text(
                        text = "DESBLOQUEADOS (${unlocked.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(unlocked) { achievement ->
                    AchievementCard(achievement = achievement, isUnlocked = true)
                }
            }

            if (locked.isNotEmpty()) {
                item {
                    Text(
                        text = "BLOQUEADOS (${locked.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                items(locked) { achievement ->
                    AchievementCard(achievement = achievement, isUnlocked = false)
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: Achievement,
    isUnlocked: Boolean
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isUnlocked) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "achievementScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale)
            .alpha(if (isUnlocked) 1f else 0.6f),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked)
                CardBackground
            else
                DarkSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono del logro
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked)
                            PrimaryBlue.copy(alpha = 0.2f)
                        else
                            Color.Gray.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = achievement.icon,
                    fontSize = 32.sp
                )
            }

            // Info del logro
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = achievement.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) TextPrimary else TextSecondary
                    )

                    if (isUnlocked) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Desbloqueado",
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = achievement.description,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (achievement.unlockedAt != null) {
                    Text(
                        text = "Desbloqueado: ${formatDate(achievement.unlockedAt)}",
                        fontSize = 11.sp,
                        color = TextTertiary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else if (achievement.maxProgress > 1) {
                    // Mostrar progreso
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            text = "${achievement.progress}/${achievement.maxProgress}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        LinearProgressIndicator(
                            progress = { achievement.progress.toFloat() / achievement.maxProgress.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = PrimaryBlue,
                            trackColor = DarkSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsContent(
    gameStats: List<GameStats>,
    achievementsManager: AchievementsManager
) {
    if (gameStats.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.Insights,
            title = "Sin estadísticas",
            message = "Empieza a jugar para ver tus estadísticas de juego"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Juegos más jugados
            item {
                Text(
                    text = "JUEGOS MÁS JUGADOS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }

            val mostPlayed = achievementsManager.getMostPlayedGames(5)
            items(mostPlayed) { stat ->
                GameStatCard(stat = stat)
            }

            // Juegos recientes
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "JUGADOS RECIENTEMENTE",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }

            val recent = achievementsManager.getRecentGames(5)
            items(recent) { stat ->
                GameStatCard(stat = stat, showLastPlayed = true)
            }
        }
    }
}

@Composable
private fun GameStatCard(
    stat: GameStats,
    showLastPlayed: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de plataforma
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                color = PrimaryBlue.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stat.platform.take(3).uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }

            // Info del juego
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stat.gameName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "⏱️ ${formatPlayTime(stat.totalPlayTime)}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Text(
                        text = "🎮 ${stat.timesOpened}x",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                if (showLastPlayed) {
                    Text(
                        text = "Hace ${formatTimeAgo(stat.lastPlayed)}",
                        fontSize = 11.sp,
                        color = TextTertiary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

private fun formatPlayTime(ms: Long): String {
    val hours = ms / (1000 * 60 * 60)
    val minutes = (ms % (1000 * 60 * 60)) / (1000 * 60)

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "< 1m"
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val minutes = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
        minutes < 60 -> "$minutes minutos"
        hours < 24 -> "$hours horas"
        days < 7 -> "$days días"
        else -> "${days / 7} semanas"
    }
}

