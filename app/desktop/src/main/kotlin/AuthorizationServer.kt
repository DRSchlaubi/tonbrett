package dev.schlaubi.tonbrett.app.desktop

import dev.schlaubi.tonbrett.app.api.Config
import dev.schlaubi.tonbrett.app.api.saveConfig
import dev.schlaubi.tonbrett.common.authServerPort
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*

fun startAuthorizationServer(reAuthorize: Boolean, onAuth: () -> Unit){
    val scope = CoroutineScope(Dispatchers.Default)
    scope.embeddedServer(Netty, port = authServerPort, module = { authModule(onAuth, scope) })
        .start(wait = !reAuthorize).stopServerOnCancellation()
}

fun Application.authModule(onAuth: () -> Unit, scope: CoroutineScope) {
    routing {
        get("login") {
            val token = call.parameters["token"] ?: throw BadRequestException("Missing token")

            saveConfig(Config(token))
            call.respond(HttpStatusCode.Gone,"You can close this tab now")
            scope.cancel()
            onAuth()
        }
    }
}
