package com.example.afo

import android.content.Context
import android.content.SharedPreferences

class FavoritesManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("emulauncher_favorites", Context.MODE_PRIVATE)

    fun isFavorite(romPath: String): Boolean {
        return prefs.getBoolean(romPath, false)
    }

    fun toggleFavorite(romPath: String): Boolean {
        val isFav = !isFavorite(romPath)
        prefs.edit().putBoolean(romPath, isFav).apply()

        // Notificar al sistema de logros
        try {
            val achievementsManager = com.example.afo.achievements.AchievementsManager.getInstance(context)
            achievementsManager.updateFavoritesCount(getAllFavorites().size)
        } catch (e: Exception) {
            // Ignorar si falla
        }

        return isFav
    }

    fun setFavorite(romPath: String, favorite: Boolean) {
        prefs.edit().putBoolean(romPath, favorite).apply()

        // Notificar al sistema de logros
        try {
            val achievementsManager = com.example.afo.achievements.AchievementsManager.getInstance(context)
            achievementsManager.updateFavoritesCount(getAllFavorites().size)
        } catch (e: Exception) {
            // Ignorar si falla
        }
    }

    fun getAllFavorites(): Set<String> {
        return prefs.all.filter { it.value as? Boolean == true }.keys
    }
}

