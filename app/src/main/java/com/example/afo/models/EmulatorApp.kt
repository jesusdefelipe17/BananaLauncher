package com.example.afo.models

data class EmulatorApp(
    val packageName: String,
    val name: String,
    val icon: Int? = null,
    val supportedPlatforms: List<Platform> = emptyList()
)

