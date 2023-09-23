package dev.schlaubi.tonbrett.app.desktop

import dev.schlaubi.tonbrett.app.api.getUrl
import dev.schlaubi.tonbrett.common.authServerPort
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

fun startAuthorizationServer(onAuth: () -> Unit) {
    val scope = CoroutineScope(Dispatchers.Default)
    scope.embeddedServer(Netty, port = authServerPort, module = { authModule(onAuth, scope) })
        .start().stopServerOnCancellation()
}

fun Application.authModule(onAuth: () -> Unit, scope: CoroutineScope) {
    install(CORS) {
        allowMethod(HttpMethod.Post)
        val baseUrl = getUrl()
        allowHost(baseUrl.host, listOf(baseUrl.protocol.name))
    }
    routing {
        post("login") {
            val token = call.parameters["token"] ?: throw BadRequestException("Missing token")

            saveConfig(Config(token))
            call.respond(HttpStatusCode.Accepted)
            // This stops the server, see stopServerOnCancellation above
            scope.cancel()
            onAuth()
        }
    }
}
