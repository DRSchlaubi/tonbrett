package dev.schlaubi.tonbrett.bot.config

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object Config : EnvironmentConfig() {
    val SOUNDS_FOLDER by getEnv(Path("sounds"), transform = ::Path)
    val MAX_SOUND_LENGTH by getEnv(30.seconds) { it.toInt().toDuration(DurationUnit.SECONDS) }
    val DISCORD_CLIENT_ID by environment
    val DISCORD_CLIENT_SECRET by environment
    val JWT_SECRET by getEnv("verrysecurenonsense")
}
