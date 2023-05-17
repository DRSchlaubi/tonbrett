package dev.schlaubi.tonbrett.bot.server

import dev.kord.core.behavior.UserBehavior
import dev.schlaubi.tonbrett.common.Event
import dev.schlaubi.tonbrett.common.Route.Me
import dev.schlaubi.tonbrett.common.Snowflake
import io.ktor.server.auth.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import mu.KotlinLogging

private val LOG = KotlinLogging.logger { }

private val sessions = mutableMapOf<Snowflake, DefaultWebSocketServerSession>()

suspend fun UserBehavior.sendEvent(event: Event) = sendEvent(id, event)

suspend fun sendEvent(id: Snowflake, event: Event) {
    val session = sessions[id] ?: return
    session.sendSerialized(event)
}

fun Route.webSocket() {
    resource<Me.Events> {
        webSocket {
            val discordUser = try {
                call.user
            } catch (e: OAuth2Exception.MissingAccessToken) {
                return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing auth information"))
            }
            sessions[discordUser.id] = this

            for (message in incoming) {
                LOG.warn { "Received unexpected message: ${message.data.contentToString()}" }
            }

            LOG.info { "Session $this got disconnected" }
            sessions.remove(discordUser.id)
        }
    }
}
