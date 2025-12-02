package com.example.afo.achievements

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String, // emoji o nombre de icono
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val progress: Int = 0,
    val maxProgress: Int = 1
)

data class GameStats(
    val gameName: String,
    val platform: String,
    val totalPlayTime: Long = 0, // en milisegundos
    val lastPlayed: Long = 0,
    val timesOpened: Int = 0,
    val firstPlayed: Long = System.currentTimeMillis()
)

class AchievementsManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("achievements_prefs", Context.MODE_PRIVATE)

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements = _achievements.asStateFlow()

    private val _gameStats = MutableStateFlow<List<GameStats>>(emptyList())
    val gameStats = _gameStats.asStateFlow()

    private val _totalPlayTime = MutableStateFlow(0L)
    val totalPlayTime = _totalPlayTime.asStateFlow()

    private val _gamesPlayed = MutableStateFlow(0)
    val gamesPlayed = _gamesPlayed.asStateFlow()

    init {
        loadAchievements()
        loadGameStats()
        initializeDefaultAchievements()
    }

    private fun initializeDefaultAchievements() {
        val defaultAchievements = listOf(
            Achievement(
                id = "first_game",
                title = "Primer Paso",
                description = "Abre tu primer juego",
                icon = "🎮",
                maxProgress = 1
            ),
            Achievement(
                id = "collector",
                title = "Coleccionista",
                description = "Ten 10 juegos en tu biblioteca",
                icon = "📚",
                maxProgress = 10
            ),
            Achievement(
                id = "time_traveler",
                title = "Viajero del Tiempo",
                description = "Juega durante 10 horas en total",
                icon = "⏰",
                maxProgress = 36000000 // 10 horas en ms
            ),
            Achievement(
                id = "marathon",
                title = "Maratonista",
                description = "Juega durante 50 horas en total",
                icon = "🏃",
                maxProgress = 180000000 // 50 horas en ms
            ),
            Achievement(
                id = "dedicated",
                title = "Dedicado",
                description = "Juega durante 7 días seguidos",
                icon = "🔥",
                maxProgress = 7
            ),
            Achievement(
                id = "variety",
                title = "Variedad",
                description = "Juega 20 juegos diferentes",
                icon = "🎲",
                maxProgress = 20
            ),
            Achievement(
                id = "favorite_five",
                title = "Los Favoritos",
                description = "Marca 5 juegos como favoritos",
                icon = "⭐",
                maxProgress = 5
            ),
            Achievement(
                id = "speed_runner",
                title = "Speedrunner",
                description = "Abre 50 juegos en total",
                icon = "⚡",
                maxProgress = 50
            ),
            Achievement(
                id = "night_owl",
                title = "Búho Nocturno",
                description = "Juega después de medianoche",
                icon = "🦉",
                maxProgress = 1
            ),
            Achievement(
                id = "early_bird",
                title = "Madrugador",
                description = "Juega antes de las 6 AM",
                icon = "🌅",
                maxProgress = 1
            )
        )

        // Solo agregar achievements que no existen
        val existingIds = _achievements.value.map { it.id }
        val newAchievements = defaultAchievements.filter { it.id !in existingIds }

        if (newAchievements.isNotEmpty()) {
            _achievements.value = _achievements.value + newAchievements
            saveAchievements()
        }
    }

    fun recordGameOpened(gameName: String, platform: String) {
        val stats = _gameStats.value.toMutableList()
        val existingIndex = stats.indexOfFirst { it.gameName == gameName }

        val currentTime = System.currentTimeMillis()

        if (existingIndex >= 0) {
            val existing = stats[existingIndex]
            stats[existingIndex] = existing.copy(
                lastPlayed = currentTime,
                timesOpened = existing.timesOpened + 1
            )
        } else {
            stats.add(
                GameStats(
                    gameName = gameName,
                    platform = platform,
                    lastPlayed = currentTime,
                    timesOpened = 1
                )
            )
        }

        _gameStats.value = stats
        _gamesPlayed.value = stats.size
        saveGameStats()

        // Verificar logros
        checkAchievements()
    }

    fun recordPlayTime(gameName: String, playTimeMs: Long) {
        val stats = _gameStats.value.toMutableList()
        val existingIndex = stats.indexOfFirst { it.gameName == gameName }

        if (existingIndex >= 0) {
            val existing = stats[existingIndex]
            stats[existingIndex] = existing.copy(
                totalPlayTime = existing.totalPlayTime + playTimeMs
            )
        }

        _gameStats.value = stats
        _totalPlayTime.value = stats.sumOf { it.totalPlayTime }
        saveGameStats()

        checkAchievements()
    }

    fun updateFavoritesCount(count: Int) {
        updateAchievementProgress("favorite_five", count)
    }

    private fun checkAchievements() {
        val stats = _gameStats.value
        val totalGames = stats.size
        val totalTime = stats.sumOf { it.totalPlayTime }
        val totalOpened = stats.sumOf { it.timesOpened }

        // Primer juego
        if (totalGames > 0) {
            unlockAchievement("first_game")
        }

        // Coleccionista
        updateAchievementProgress("collector", totalGames)

        // Tiempo de juego
        updateAchievementProgress("time_traveler", totalTime.toInt())
        updateAchievementProgress("marathon", totalTime.toInt())

        // Variedad
        updateAchievementProgress("variety", totalGames)

        // Speedrunner
        updateAchievementProgress("speed_runner", totalOpened)

        // Verificar hora del día
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        if (hour >= 0 && hour < 6) {
            unlockAchievement("early_bird")
        }
        if (hour >= 0 && hour < 6 || hour >= 22) {
            unlockAchievement("night_owl")
        }

        // Verificar días consecutivos
        checkConsecutiveDays()
    }

    private fun checkConsecutiveDays() {
        val lastPlayedDate = prefs.getLong("last_played_date", 0)
        val today = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
        val lastDay = lastPlayedDate / (1000 * 60 * 60 * 24)

        if (today - lastDay == 1L) {
            // Día consecutivo
            val streak = prefs.getInt("play_streak", 0) + 1
            prefs.edit().putInt("play_streak", streak).apply()
            updateAchievementProgress("dedicated", streak)
        } else if (today > lastDay + 1) {
            // Se rompió la racha
            prefs.edit().putInt("play_streak", 1).apply()
        }

        prefs.edit().putLong("last_played_date", System.currentTimeMillis()).apply()
    }

    private fun updateAchievementProgress(achievementId: String, progress: Int) {
        val achievements = _achievements.value.toMutableList()
        val index = achievements.indexOfFirst { it.id == achievementId }

        if (index >= 0) {
            val achievement = achievements[index]
            if (!achievement.isUnlocked && progress >= achievement.maxProgress) {
                achievements[index] = achievement.copy(
                    isUnlocked = true,
                    progress = achievement.maxProgress,
                    unlockedAt = System.currentTimeMillis()
                )
                _achievements.value = achievements
                saveAchievements()
            } else if (!achievement.isUnlocked) {
                achievements[index] = achievement.copy(progress = progress)
                _achievements.value = achievements
                saveAchievements()
            }
        }
    }

    private fun unlockAchievement(achievementId: String) {
        val achievements = _achievements.value.toMutableList()
        val index = achievements.indexOfFirst { it.id == achievementId }

        if (index >= 0 && !achievements[index].isUnlocked) {
            achievements[index] = achievements[index].copy(
                isUnlocked = true,
                progress = achievements[index].maxProgress,
                unlockedAt = System.currentTimeMillis()
            )
            _achievements.value = achievements
            saveAchievements()
        }
    }

    fun getUnlockedAchievements(): List<Achievement> {
        return _achievements.value.filter { it.isUnlocked }
    }

    fun getUnlockedPercentage(): Float {
        val total = _achievements.value.size
        if (total == 0) return 0f
        val unlocked = _achievements.value.count { it.isUnlocked }
        return (unlocked.toFloat() / total.toFloat()) * 100f
    }

    fun getMostPlayedGames(limit: Int = 5): List<GameStats> {
        return _gameStats.value
            .sortedByDescending { it.totalPlayTime }
            .take(limit)
    }

    fun getRecentGames(limit: Int = 5): List<GameStats> {
        return _gameStats.value
            .sortedByDescending { it.lastPlayed }
            .take(limit)
    }

    private fun saveAchievements() {
        val jsonArray = JSONArray()
        _achievements.value.forEach { achievement ->
            val jsonObject = JSONObject().apply {
                put("id", achievement.id)
                put("title", achievement.title)
                put("description", achievement.description)
                put("icon", achievement.icon)
                put("isUnlocked", achievement.isUnlocked)
                put("unlockedAt", achievement.unlockedAt ?: 0)
                put("progress", achievement.progress)
                put("maxProgress", achievement.maxProgress)
            }
            jsonArray.put(jsonObject)
        }
        prefs.edit().putString("achievements", jsonArray.toString()).apply()
    }

    private fun loadAchievements() {
        val json = prefs.getString("achievements", null) ?: return
        try {
            val jsonArray = JSONArray(json)
            val achievements = mutableListOf<Achievement>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                achievements.add(
                    Achievement(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        description = obj.getString("description"),
                        icon = obj.getString("icon"),
                        isUnlocked = obj.getBoolean("isUnlocked"),
                        unlockedAt = if (obj.getLong("unlockedAt") == 0L) null else obj.getLong("unlockedAt"),
                        progress = obj.getInt("progress"),
                        maxProgress = obj.getInt("maxProgress")
                    )
                )
            }
            _achievements.value = achievements
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveGameStats() {
        val jsonArray = JSONArray()
        _gameStats.value.forEach { stat ->
            val jsonObject = JSONObject().apply {
                put("gameName", stat.gameName)
                put("platform", stat.platform)
                put("totalPlayTime", stat.totalPlayTime)
                put("lastPlayed", stat.lastPlayed)
                put("timesOpened", stat.timesOpened)
                put("firstPlayed", stat.firstPlayed)
            }
            jsonArray.put(jsonObject)
        }
        prefs.edit().putString("game_stats", jsonArray.toString()).apply()

        // Actualizar totales
        _totalPlayTime.value = _gameStats.value.sumOf { it.totalPlayTime }
        _gamesPlayed.value = _gameStats.value.size
    }

    private fun loadGameStats() {
        val json = prefs.getString("game_stats", null) ?: return
        try {
            val jsonArray = JSONArray(json)
            val stats = mutableListOf<GameStats>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                stats.add(
                    GameStats(
                        gameName = obj.getString("gameName"),
                        platform = obj.getString("platform"),
                        totalPlayTime = obj.getLong("totalPlayTime"),
                        lastPlayed = obj.getLong("lastPlayed"),
                        timesOpened = obj.getInt("timesOpened"),
                        firstPlayed = obj.getLong("firstPlayed")
                    )
                )
            }
            _gameStats.value = stats
            _totalPlayTime.value = stats.sumOf { it.totalPlayTime }
            _gamesPlayed.value = stats.size
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        @Volatile
        private var instance: AchievementsManager? = null

        fun getInstance(context: Context): AchievementsManager {
            return instance ?: synchronized(this) {
                instance ?: AchievementsManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

