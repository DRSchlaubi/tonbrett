package dev.schlaubi.tonbrett.bot.server

import dev.schlaubi.mikbot.util_plugins.ktor.api.KtorExtensionPoint
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.modules.SerializersModule
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule
import org.pf4j.Extension

@Extension
class Ktor : KtorExtensionPoint {
    override fun Application.apply() {
        installAuth()
        routing {
            files()
            authenticated {
                sounds()
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

