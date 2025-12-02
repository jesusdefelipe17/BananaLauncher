package com.example.afo.models

data class RomFile(
    val name: String,
    val path: String,
    val coverPath: String? = null,
    val platform: Platform,
    var isFavorite: Boolean = false
)

