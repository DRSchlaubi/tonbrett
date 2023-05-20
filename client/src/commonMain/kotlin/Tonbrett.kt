package dev.schlaubi.tonbrett.client

import dev.schlaubi.tonbrett.common.Event
import dev.schlaubi.tonbrett.common.Route
import dev.schlaubi.tonbrett.common.Sound
import dev.schlaubi.tonbrett.common.User
import mu.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json

private val LOG = KotlinLogging.logger { }

class Tonbrett(private val token: String, private val baseUrl: Url) {
    private val eventFlow = MutableSharedFlow<Event>()
    private val client = HttpClient {
        expectSuccess = true
        install(ContentNegotiation) {
            json()
        }
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        install(Resources)

        defaultRequest {
            bearerAuth(token)
            url.takeFrom(baseUrl)
        }
    }
    val events = eventFlow.asSharedFlow()

    suspend fun getSounds(onlyMine: Boolean = false, query: String? = null): List<Sound> =
        client.get(Route.Sounds(onlyMine, query)).body()

    suspend fun getMe(): User = client.get(Route.Me()).body()

    suspend fun play(soundId: String): Unit = client.post(Route.Sounds.Sound.Play(soundId)).body()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Suppress("INVISIBLE_MEMBER")
    suspend fun connect() {
        val session = client.webSocketSession {
            href(client.resources().resourcesFormat, Route.Me.Events(token), url)
            url {
                protocol = if (baseUrl.protocol.isSecure()) URLProtocol.WSS else URLProtocol.WS
                port = protocol.defaultPort
            }
        }

        while (!session.incoming.isClosedForReceive) {
            try {
                eventFlow.emit(session.receiveDeserialized())
            } catch (e: WebsocketDeserializeException) {
                LOG.warn(e) { "Could not deserialize incoming ws packet" }
            }
        }
    }
}