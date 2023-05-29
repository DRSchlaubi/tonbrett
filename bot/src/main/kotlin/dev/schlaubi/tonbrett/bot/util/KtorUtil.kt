package dev.schlaubi.tonbrett.bot.util

import dev.schlaubi.mikbot.plugin.api.pluginSystem
import io.ktor.server.application.*
import io.ktor.server.plugins.*

fun notFound(message: String): Nothing = throw NotFoundException(message)
fun soundNotFound(): Nothing = notFound("Sound not found")

fun badRequest(message: String): Nothing = throw BadRequestException(message)

fun ApplicationCall.translate(key: String, vararg replacements: String): String {
    @Suppress("DEPRECATION", "UNCHECKED_CAST")
    return pluginSystem.translate(key, "soundboard", null, replacements as Array<Any?>)
}
