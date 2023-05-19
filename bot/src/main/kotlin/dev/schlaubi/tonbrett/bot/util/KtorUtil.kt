package dev.schlaubi.tonbrett.bot.util

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.koin.KordExContext
import dev.schlaubi.mikbot.plugin.api.pluginSystem
import dev.schlaubi.tonbrett.bot.server.user
import io.ktor.server.application.*
import io.ktor.server.plugins.*

private val bot by KordExContext.get().inject<ExtensibleBot>()

fun notFound(message: String): Nothing = throw NotFoundException(message)
fun soundNotFound(): Nothing = notFound("Sound not found")

fun badRequest(message: String): Nothing = throw BadRequestException(message)

fun ApplicationCall.translate(key: String, vararg replacements: String): String {
    val language = user.locale.value

    @Suppress("DEPRECATION")
    return pluginSystem.translate(key, "soundboard", language, replacements as Array<Any?>)
}
