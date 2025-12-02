package com.example.afo

import android.content.Context
import android.content.SharedPreferences

class FavoritesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("emulauncher_favorites", Context.MODE_PRIVATE)

    fun isFavorite(romPath: String): Boolean {
        return prefs.getBoolean(romPath, false)
    }

    fun toggleFavorite(romPath: String): Boolean {
        val isFav = !isFavorite(romPath)
        prefs.edit().putBoolean(romPath, isFav).apply()
        return isFav
    }

    fun setFavorite(romPath: String, favorite: Boolean) {
        prefs.edit().putBoolean(romPath, favorite).apply()
    }

    fun getAllFavorites(): Set<String> {
        return prefs.all.filter { it.value as? Boolean == true }.keys
    }
}

