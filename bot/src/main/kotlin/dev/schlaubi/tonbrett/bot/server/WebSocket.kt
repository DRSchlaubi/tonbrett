package dev.schlaubi.tonbrett.bot.server

import com.auth0.jwt.exceptions.JWTVerificationException
import dev.schlaubi.tonbrett.bot.util.badRequest
import dev.schlaubi.tonbrett.common.*
import dev.schlaubi.tonbrett.common.Route.Me
import dev.schlaubi.tonbrett.common.util.convertForNonJvmPlatforms
import dev.schlaubi.tonbrett.common.Snowflake
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.routing.Route
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import dev.kord.common.entity.Snowflake as snowflake

private val LOG = KotlinLogging.logger { }

class WebSocketSession(
    val useUnicode: Boolean,
    val delegate: DefaultWebSocketServerSession
) : DefaultWebSocketServerSession by delegate {
    suspend inline fun <reified T : Event> sendEvent(event: T) {
        val updatedEvent = if (event is HasSound && useUnicode) {
            event.withSound(event.sound.convertForNonJvmPlatforms())
        } else {
            event
        }

        sendSerialized(updatedEvent)
    }
}

private val sessions = mutableMapOf<Snowflake, WebSocketSession>()

suspend fun sendEvent(id: Snowflake, event: Event) {
    val session = sessions[id] ?: return
    session.sendSerialized(event)
}

suspend fun broadcastEvent(event: Event) = coroutineScope {
    sessions.forEach { (_, session) ->
        launch {
            session.sendEvent(event)
        }
    }
}

private val DISCORD_USER = AttributeKey<Snowflake>("DISCORD_USER")

fun Route.webSocket() {
    resource<Me.Events> {
        intercept(ApplicationCallPipeline.Plugins) {
            val discordUser = try {
                val sessionId = call.parameters["session_token"] ?: badRequest("Missing auth information")
                snowflake(verifyJwt(sessionId).getClaim("userId").asLong())
            } catch (e: JWTVerificationException) {
                call.respond(HttpStatusCode.Unauthorized)
                finish()
                return@intercept
            }

            call.attributes.put(DISCORD_USER, discordUser)
            proceed()
        }
        webSocket {
            val discordUser = call.attributes[DISCORD_USER]

            sessions.remove(discordUser)?.close(
                CloseReason(
                    CloseReason.Codes.VIOLATED_POLICY,
                    "Connected from another location"
                )
            )
            val useUnicode = call.parameters["use_unicode"].toBoolean()
            sessions[discordUser] = WebSocketSession(useUnicode, this)

            for (message in incoming) {
                LOG.warn { "Received unexpected message: ${message.data.contentToString()}" }
            }

            LOG.info { "Session $this got disconnected" }
            sessions.remove(discordUser)
        }
    }
}
