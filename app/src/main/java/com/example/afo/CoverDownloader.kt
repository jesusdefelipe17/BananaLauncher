package com.example.afo

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLEncoder

class CoverDownloader(private val context: Context) {

    private val coverDir = File(context.filesDir, "covers")

    init {
        if (!coverDir.exists()) {
            coverDir.mkdirs()
        }
    }

    /**
     * Descarga una carátula para un juego desde múltiples fuentes
     */
    suspend fun downloadCover(romName: String, platform: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Limpiar nombre del juego para la búsqueda
                val cleanName = cleanRomName(romName)
                val fileName = "${cleanName}_${platform}.jpg"
                val coverFile = File(coverDir, fileName)

                // Si ya existe, retornar la ruta
                if (coverFile.exists() && coverFile.length() > 0) {
                    Log.d("CoverDownloader", "Cover already exists: ${coverFile.absolutePath}")
                    return@withContext coverFile.absolutePath
                }

                // Intentar descargar desde SteamGridDB
                val coverUrl = downloadFromSteamGridDB(cleanName, platform)
                if (coverUrl != null) {
                    try {
                        Log.d("CoverDownloader", "Downloading from SteamGridDB: $coverUrl")
                        val url = URL(coverUrl)
                        val connection = url.openConnection()
                        connection.connectTimeout = 10000
                        connection.readTimeout = 10000

                        val inputStream = connection.getInputStream()
                        val outputStream = FileOutputStream(coverFile)

                        inputStream.copyTo(outputStream)

                        outputStream.close()
                        inputStream.close()

                        // Verificar que el archivo descargado es válido
                        if (coverFile.exists() && coverFile.length() > 1024) {
                            Log.d("CoverDownloader", "Cover downloaded successfully: ${coverFile.absolutePath}")
                            return@withContext coverFile.absolutePath
                        } else {
                            coverFile.delete()
                        }
                    } catch (e: Exception) {
                        Log.e("CoverDownloader", "Failed to download from SteamGridDB: ${e.message}")
                    }
                }

                // Si SteamGridDB falla, intentar otras fuentes
                val fallbackSources = listOf(
                    "https://cdn.thegamesdb.net/images/original/boxart/front/${URLEncoder.encode(cleanName, "UTF-8")}.jpg",
                    "https://www.mobygames.com/images/covers/l/${URLEncoder.encode(cleanName, "UTF-8")}.jpg"
                )

                for (sourceUrl in fallbackSources) {
                    try {
                        Log.d("CoverDownloader", "Trying fallback source: $sourceUrl")
                        val url = URL(sourceUrl)
                        val connection = url.openConnection()
                        connection.connectTimeout = 5000
                        connection.readTimeout = 5000

                        val inputStream = connection.getInputStream()
                        val outputStream = FileOutputStream(coverFile)

                        inputStream.copyTo(outputStream)

                        outputStream.close()
                        inputStream.close()

                        if (coverFile.exists() && coverFile.length() > 1024) {
                            Log.d("CoverDownloader", "Cover downloaded from fallback: ${coverFile.absolutePath}")
                            return@withContext coverFile.absolutePath
                        } else {
                            coverFile.delete()
                        }
                    } catch (e: Exception) {
                        Log.e("CoverDownloader", "Failed fallback source: ${e.message}")
                        continue
                    }
                }

                // Si no se pudo descargar, crear un placeholder local
                createLocalPlaceholder(coverFile, platform)
                return@withContext coverFile.absolutePath

            } catch (e: Exception) {
                Log.e("CoverDownloader", "Error downloading cover: ${e.message}", e)
                null
            }
        }
    }

    /**
     * Limpia el nombre del ROM para la búsqueda
     */
    private fun cleanRomName(name: String): String {
        return name
            .replace(Regex("\\[.*?\\]"), "") // Quitar [tags]
            .replace(Regex("\\(.*?\\)"), "") // Quitar (tags)
            .replace(Regex("[^a-zA-Z0-9\\s]"), "") // Solo letras, números y espacios
            .trim()
            .replace(Regex("\\s+"), " ") // Normalizar espacios
    }

    /**
     * Busca y obtiene la URL de la carátula desde SteamGridDB
     */
    private suspend fun downloadFromSteamGridDB(gameName: String, platform: String): String? {
        return try {
            val apiKey = "bf46200298cbafc25fdb479ff14cec72"

            // Paso 1: Buscar el juego
            val searchUrl = "https://www.steamgriddb.com/api/v2/search/autocomplete/${URLEncoder.encode(gameName, "UTF-8")}"
            Log.d("CoverDownloader", "Searching game: $searchUrl")

            val searchConnection = URL(searchUrl).openConnection()
            searchConnection.setRequestProperty("Authorization", "Bearer $apiKey")
            searchConnection.connectTimeout = 10000
            searchConnection.readTimeout = 10000

            val searchResponse = searchConnection.getInputStream().bufferedReader().use { it.readText() }
            Log.d("CoverDownloader", "Search response: $searchResponse")

            // Parsear manualmente el JSON para obtener el game ID
            val gameId = extractGameId(searchResponse)
            if (gameId == null) {
                Log.d("CoverDownloader", "No game ID found in response")
                return null
            }

            Log.d("CoverDownloader", "Found game ID: $gameId")

            // Paso 2: Obtener las grids/covers del juego
            val gridsUrl = "https://www.steamgriddb.com/api/v2/grids/game/$gameId"
            Log.d("CoverDownloader", "Getting grids: $gridsUrl")

            val gridsConnection = URL(gridsUrl).openConnection()
            gridsConnection.setRequestProperty("Authorization", "Bearer $apiKey")
            gridsConnection.connectTimeout = 10000
            gridsConnection.readTimeout = 10000

            val gridsResponse = gridsConnection.getInputStream().bufferedReader().use { it.readText() }

            // Extraer la URL de la primera imagen
            val imageUrl = extractImageUrl(gridsResponse)
            Log.d("CoverDownloader", "Found image URL: $imageUrl")

            imageUrl
        } catch (e: Exception) {
            Log.e("CoverDownloader", "Error with SteamGridDB: ${e.message}", e)
            null
        }
    }

    /**
     * Extrae el game ID del JSON de respuesta
     */
    private fun extractGameId(json: String): Int? {
        return try {
            // Buscar "id": seguido de un número
            val idPattern = """"id"\s*:\s*(\d+)""".toRegex()
            val match = idPattern.find(json)
            match?.groupValues?.get(1)?.toIntOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extrae la URL de la imagen del JSON de respuesta
     */
    private fun extractImageUrl(json: String): String? {
        return try {
            // Buscar "url": "https://..."
            val urlPattern = """"url"\s*:\s*"([^"]+)"""".toRegex()
            val match = urlPattern.find(json)
            match?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtiene URL de placeholder según plataforma
     */
    private fun getPlaceholderUrl(platform: String): String {
        // URLs de placeholders genéricos por plataforma
        return when (platform.uppercase()) {
            "PSP" -> "https://via.placeholder.com/272x408/000000/FFFFFF?text=PSP"
            "PSX", "PS1" -> "https://via.placeholder.com/272x408/003791/FFFFFF?text=PS1"
            "PS2" -> "https://via.placeholder.com/272x408/0070CC/FFFFFF?text=PS2"
            "SWITCH", "NSW" -> "https://via.placeholder.com/272x408/E60012/FFFFFF?text=Switch"
            "GBA" -> "https://via.placeholder.com/272x408/6B4C9A/FFFFFF?text=GBA"
            "NDS" -> "https://via.placeholder.com/272x408/D12228/FFFFFF?text=NDS"
            "N64" -> "https://via.placeholder.com/272x408/0E4C92/FFFFFF?text=N64"
            "SNES" -> "https://via.placeholder.com/272x408/8F8F8F/FFFFFF?text=SNES"
            "NES" -> "https://via.placeholder.com/272x408/E60012/FFFFFF?text=NES"
            else -> "https://via.placeholder.com/272x408/666666/FFFFFF?text=Game"
        }
    }

    /**
     * Crea un placeholder local cuando no se puede descargar
     */
    private fun createLocalPlaceholder(file: File, platform: String) {
        // Aquí podrías generar una imagen bitmap local con el nombre de la plataforma
        // Por ahora, simplemente no creamos nada y el sistema usará el placeholder de Compose
        Log.d("CoverDownloader", "Using local placeholder for $platform")
    }

    /**
     * Limpia el caché de carátulas
     */
    fun clearCache() {
        coverDir.listFiles()?.forEach { it.delete() }
    }

    /**
     * Obtiene el tamaño del caché en MB
     */
    fun getCacheSize(): Double {
        val totalSize = coverDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
        return totalSize / (1024.0 * 1024.0)
    }
}

