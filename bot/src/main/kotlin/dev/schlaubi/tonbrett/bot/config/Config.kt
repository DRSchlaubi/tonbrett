package dev.schlaubi.tonbrett.bot.config

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig
import kotlin.io.path.Path

object Config : EnvironmentConfig() {
    val SOUNDS_FOLDER by getEnv(Path("sounds"), transform = ::Path)
}
