package dev.schlaubi.tonbrett.bot.server

import dev.schlaubi.tonbrett.bot.util.badRequest
import dev.schlaubi.tonbrett.common.Event
import dev.schlaubi.tonbrett.common.Route.Me
import dev.schlaubi.tonbrett.common.Snowflake
import io.ktor.server.plugins.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val LOG = KotlinLogging.logger { }

private val sessions = mutableMapOf<Snowflake, DefaultWebSocketServerSession>()

suspend fun sendEvent(id: Snowflake, event: Event) {
    val session = sessions[id] ?: return
    session.sendSerialized(event)
}

suspend fun broadcastEvent(event: Event) = coroutineScope {
    sessions.forEach { (_, session) ->
        launch {
            session.sendSerialized(event)
        }
    }
}

fun Route.webSocket() {
    resource<Me.Events> {
        webSocket socket@{
            val discordUser = try {
                val sessionId = call.parameters["session_token"] ?: badRequest("Missing auth information")
                findSession(sessionId) ?: badRequest("Invalid auth info")
            } catch (e: BadRequestException) {
                return@socket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, e.message!!))
            }
            sessions.remove(discordUser.id)?.close(
                CloseReason(
                    CloseReason.Codes.VIOLATED_POLICY,
                    "Connected from another location"
                )
            )
            sessions[discordUser.id] = this

            for (message in incoming) {
                LOG.warn { "Received unexpected message: ${message.data.contentToString()}" }
            }

            LOG.info { "Session $this got disconnected" }
            sessions.remove(discordUser.id)
        }
    }
}
