package dev.schlaubi.tonbrett.app.desktop

import dev.schlaubi.tonbrett.app.api.Config
import dev.schlaubi.tonbrett.app.api.saveConfig
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun startAuthorizationServer(reAuthorize: Boolean, onAuth: () -> Unit) =
    embeddedServer(Netty, port = 12548, module = { authModule(onAuth) }).start(wait = !reAuthorize)

fun Application.authModule(onAuth: () -> Unit) {
    routing {
        get("login") {
            val token = call.parameters["token"] ?: throw BadRequestException("Missing token")

            saveConfig(Config(token))
            call.respond("You can close this tab now")
            (application.environment as ApplicationEngineEnvironment).stop()
            onAuth()
        }
    }
}
