package dev.schlaubi.tonbrett.client

import dev.schlaubi.tonbrett.common.*
import io.github.oshai.kotlinlogging.KotlinLogging
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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json

private val LOG = KotlinLogging.logger { }

private val AUTHORIZE_REQUEST = AttributeKey<Boolean>("AUTHORIZE_REQUEST")
private var HttpRequestBuilder.authorize: Boolean
    get() = attributes.getOrNull(AUTHORIZE_REQUEST) ?: true
    set(value) = attributes.put(AUTHORIZE_REQUEST, value)

private val USE_UNICODE = AttributeKey<Boolean>("USE_UNICODE")
private var HttpRequestBuilder.useUnicode: Boolean
    get() = attributes[USE_UNICODE]
    set(value) = attributes.put(USE_UNICODE, value)

/**
 * Exception thrown if the automated token refresh failed and a new authentication is required.
 */
class ReauthorizationRequiredException : Exception()

/**
 * API client for the Tonbrett api.
 *
 * @property events a flow of [Events][Event] received
 * @param initialToken the initial API token to authenticate
 * @param baseUrl the url of the API
 * @param onTokenRefresh a callback invoked if the token got refreshed
 */
class Tonbrett(
    initialToken: String,
    @PublishedApi internal val baseUrl: Url,
    private val onTokenRefresh: (String) -> Unit = {}
) {
    private val eventFlow = MutableSharedFlow<Event>()
    private val json = Json {
        serializersModule = TonbrettSerializersModule
    }
    private val client = HttpClient {
        expectSuccess = true
        install(ContentNegotiation) {
            json(json)
        }
        install(WebSocketRetry) {
            maxRetries = 5
            exponentialDelay()
        }
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(json)
        }
        install(Resources)
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(accessToken = initialToken, refreshToken = initialToken)
                }
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
                            val token =
                                parseAuthorizationHeader(headers[HttpHeaders.Authorization]!!) as HttpAuthHeader.Single
                            href(Route.Me.Events(token.blob, useUnicode), url)
                            url {
                                protocol = if (baseUrl.protocol.isSecure()) URLProtocol.WSS else URLProtocol.WS
                                port = baseUrl.port
                                pathSegments = baseUrl.pathSegments + url.pathSegments
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

    /**
     * Retrieves all available sounds as [groups][SoundGroup].
     *
     * @param onlyMine whether to show only the current users sounds
     * @param query an optional search query
     * @param useUnicode whether to return the emojis as unicode or Twemoji URLs
     */
    suspend fun getSounds(
        onlyMine: Boolean = false,
        query: String? = null,
        useUnicode: Boolean = false
    ): List<SoundGroup> =
        client.get(Route.Sounds.ListSounds(onlyMine, query, useUnicode)).body()

    /**
     * Retrieves all available sounds as [sounds][Sound].
     *
     * @param onlyMine whether to show only the current users sounds
     * @param query an optional search query
     * @param useUnicode whether to return the emojis as unicode or Twemoji URLs
     */
    suspend fun getSoundList(onlyMine: Boolean = false, query: String? = null, useUnicode: Boolean = false) =
        getSounds(onlyMine, query, useUnicode).flatMap(SoundGroup::sounds)

    /**
     * Returns the current [User].
     */
    suspend fun getMe(): User = client.get(Route.Me()).body()

    /**
     * Plays the sound by its [id][soundId].
     */
    suspend fun play(soundId: String): Unit = client.post(Route.Sounds.Sound.Play(soundId)).body()

    /**
     * Retrieves the tags matching [query].
     *
     * @param limit an optional limit
     */
    suspend fun getTags(query: String? = null, limit: Int? = 0): List<String> =
        client.get(Route.Tags(query, limit)).body()

    /**
     * Stops playback of the current sound.
     */
    suspend fun stop(): Unit = client.post(Route.StopPlayer()).body()

    /**
     * Constructs an URL to [resource].
     */
    inline fun <reified T> href(resource: T): String {
        val builder = URLBuilder(baseUrl)
        href(resource, builder)
        return builder.toString()
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun connect(useUnicode: Boolean = false) {
        client.retryingWebSocket({ this.useUnicode = useUnicode }) {
            while (!incoming.isClosedForReceive) {
                try {
                    val event = receiveDeserialized<Event>()
                    LOG.debug { "Received event: $event" }
                    eventFlow.emit(event)
                } catch (e: WebsocketDeserializeException) {
                    LOG.warn(e) { "Could not deserialize incoming ws packet" }
                }
            }
        }
    }
}

/**
 * Updates the [urlBuilder] to [resource].
 */
@Suppress("INVISIBLE_MEMBER")
inline fun <reified T> HttpClient.href(resource: T, urlBuilder: URLBuilder) =
    href(plugin(Resources).resourcesFormat, resource, urlBuilder)
