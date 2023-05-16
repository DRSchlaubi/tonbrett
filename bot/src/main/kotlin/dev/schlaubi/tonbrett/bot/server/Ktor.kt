package dev.schlaubi.tonbrett.bot.server

import dev.schlaubi.mikbot.util_plugins.ktor.api.KtorExtensionPoint
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.pf4j.Extension

@Extension
class Ktor : KtorExtensionPoint {
    override fun Application.apply() {
        routing {
            files()
        }
    }
}
