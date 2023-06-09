package dev.schlaubi.tonbrett.client

import dev.schlaubi.tonbrett.common.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.api.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.resources.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val LOG = KotlinLogging.logger { }

private val AUTHORIZE_REQUEST = AttributeKey<Boolean>("AUTHORIZE_REQUEST")
private var HttpRequestBuilder.authorize: Boolean
    get() = attributes.getOrNull(AUTHORIZE_REQUEST) ?: true
    set(value) = attributes.put(AUTHORIZE_REQUEST, value)

private val USE_UNICODE = AttributeKey<Boolean>("USE_UNICODE")
private var HttpRequestBuilder.useUnicode: Boolean
    get() = attributes[USE_UNICODE]
    set(value) = attributes.put(USE_UNICODE, value)

class ReauthorizationRequiredException : Exception()

class Tonbrett(initialToken: String, private val baseUrl: Url, private val onTokenRefresh: (String) -> Unit = {}) {
    private val eventFlow = MutableSharedFlow<Event>()
    private val json = Json {
        serializersModule = TonbrettSerializersModule
    }
    private val client = HttpClient {
        expectSuccess = true
        install(ContentNegotiation) {
            json(json)
        }
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(json)
        }
        install(Resources)
        install(Auth) {
            bearer {
                loadTokens { BearerTokens(accessToken = initialToken, refreshToken =  initialToken) }
                sendWithoutRequest { it.authorize }
                refreshTokens {
                    val response = client.post(Route.Auth.Refresh()) {
                        expectSuccess = false
                        authorize = false
                        contentType(ContentType.Application.Json)
                        setBody(AuthRefreshRequest(expiredJwt = oldTokens!!.refreshToken))
                    }
                    if (!response.status.isSuccess()) {
                        throw ReauthorizationRequiredException()
                    }
                    val (newJwt) = response.body<AuthRefreshResponse>()
                    onTokenRefresh(newJwt)
                    BearerTokens(accessToken = newJwt, refreshToken = newJwt)
                }
            }
        }
        val webSocketAuth = createClientPlugin("WebSocketAuth") {
            onRequest { builder, _ ->
                with(builder) {
                    when (url.protocol) {
                        URLProtocol.WSS, URLProtocol.WS -> {
                            val token = parseAuthorizationHeader(headers[HttpHeaders.Authorization]!!) as HttpAuthHeader.Single
                            href(Route.Me.Events(token.blob, useUnicode), url)
                            url {
                                protocol = if (baseUrl.protocol.isSecure()) URLProtocol.WSS else URLProtocol.WS
                                port = baseUrl.port
                            }
                        }
                    }
                }
            }
        }
        install(webSocketAuth) // must be installed after Auth

        defaultRequest {
            url.takeFrom(baseUrl)
        }
    }
    val events = eventFlow.asSharedFlow()

    suspend fun getSounds(onlyMine: Boolean = false, query: String? = null, useUnicode: Boolean = false): List<Sound> =
        client.get(Route.Sounds.ListSounds(onlyMine, query, useUnicode)).body()

    suspend fun getMe(): User = client.get(Route.Me()).body()

    suspend fun play(soundId: String): Unit = client.post(Route.Sounds.Sound.Play(soundId)).body()

    suspend fun getTags(query: String? = null, limit: Int? = 0): List<String> =
        client.get(Route.Tags(query, limit)).body()

    suspend fun stop(): Unit = client.post(Route.StopPlayer()).body()

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun connect(useUnicode: Boolean = false) {
        val session = client.webSocketSession {
            this.useUnicode = useUnicode
        }
        while (!session.incoming.isClosedForReceive) {
            try {
                val event = session.receiveDeserialized<Event>()
                LOG.debug { "Received event: $event" }
                eventFlow.emit(event)
            } catch (e: WebsocketDeserializeException) {
                LOG.warn(e) { "Could not deserialize incoming ws packet" }
            } catch (e: EOFException) {
                LOG.warn(e) { "Websocket connection closed unexpectedly" }
            } catch (e: ClosedReceiveChannelException) {
                LOG.warn(e) { "Websocket connection closed unexpectedly" }
            }
        }
    }
}

@Suppress("INVISIBLE_MEMBER")
inline fun <reified T> HttpClient.href(resource: T, urlBuilder: URLBuilder) =
    href(resources().resourcesFormat, resource, urlBuilder)
