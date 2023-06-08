@file:OptIn(ExperimentalSerializationApi::class)

package dev.schlaubi.tonbrett.app.api

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.*

@Serializable
data class Config(@EncodeDefault(EncodeDefault.Mode.NEVER) val sessionToken: String? = null)

fun getAppDataFolder(): Path {
    val os = System.getProperty("os.name")
    val basePath = when {
        os.contains("windows", ignoreCase = true) ->
            Path(System.getenv("APPDATA"))
        os.contains("mac", ignoreCase = true) ->
            Path(System.getenv("HOME")) / "Library" / "Application Support"
        else -> Path(System.getProperty("user.home"))
    }
    return basePath / "Tonbrett"
}

fun getConfigFile() = getAppDataFolder() / "config.json"

fun getConfig(): Config {
    val file = getConfigFile()
    return if (file.exists()) {
        Json.decodeFromStream<Config>(file.inputStream())
    } else {
        Config()
    }
}

fun saveConfig(config: Config) {
    val file = getConfigFile()
    if (!file.parent.exists()) {
        file.parent.createDirectories()
    }

    Json.encodeToStream(config, getConfigFile().outputStream(StandardOpenOption.CREATE, StandardOpenOption.WRITE))
}
