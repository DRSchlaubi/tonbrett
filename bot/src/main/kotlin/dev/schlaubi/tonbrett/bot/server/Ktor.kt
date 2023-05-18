package dev.schlaubi.tonbrett.bot.server

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.schlaubi.mikbot.plugin.api.config.Environment
import dev.schlaubi.mikbot.util_plugins.ktor.api.KtorExtensionPoint
import dev.schlaubi.mikbot.plugin.api.config.Config as BotConfig
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule
import org.pf4j.Extension

@Extension
class Ktor : KtorExtensionPoint, KordExKoinComponent {
    override fun Application.apply() {
        install(WebSockets) {
            val json = Json {
                serializersModule = IdKotlinXSerializationModule
            }
            contentConverter = KotlinxWebsocketSerializationConverter(json)
        }
        installAuth()
        if (BotConfig.ENVIRONMENT == Environment.DEVELOPMENT) {
            install(CORS) {
                allowMethod(HttpMethod.Options)
                allowMethod(HttpMethod.Put)
                allowMethod(HttpMethod.Delete)
                allowMethod(HttpMethod.Patch)
                allowHeader(HttpHeaders.Authorization)
                anyHost()
            }
        }
        routing {
            files()
            ui()
            authenticated {
                sounds()
                users()
                webSocket()
            }
        }

    }

    override fun provideSerializersModule(): SerializersModule =
        IdKotlinXSerializationModule

    override fun StatusPagesConfig.apply() {
        exception<BadRequestException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "")
        }
        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, cause.message ?: "")
        }
        exception<OAuth2Exception> { call: ApplicationCall, cause: OAuth2Exception ->
            call.respond(HttpStatusCode.Unauthorized, cause.errorCode ?: "")
        }
    }
}

