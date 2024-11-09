@file:OptIn(InternalAPI::class)

package dev.schlaubi.tonbrett.bot.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import dev.kord.common.KordConfiguration
import dev.kord.common.entity.DiscordUser
import dev.schlaubi.mikbot.plugin.api.InternalAPI
import dev.schlaubi.mikbot.util_plugins.ktor.api.buildBotUrl
import dev.schlaubi.tonbrett.bot.config.Config
import dev.schlaubi.tonbrett.bot.util.badRequest
import dev.schlaubi.tonbrett.common.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import dev.kord.common.entity.Snowflake as snowflake
import dev.schlaubi.mikbot.util_plugins.ktor.api.Config as KtorConfig
import io.ktor.server.routing.Route as KtorRoute

private const val discordAuth = "discord"
private const val jwtAuth = "jwt"

@Serializable
data class Session(val type: Route.Auth.Type)

private val jwtVerifier = JWT
    .require(Algorithm.HMAC256(Config.JWT_SECRET))
    .withIssuer(KtorConfig.WEB_SERVER_URL.toString())
    .build()

fun verifyJwt(jwt: String): DecodedJWT = jwtVerifier.verify(jwt)

private val httpClient = HttpClient {
    install(ContentNegotiation) {
        val json = Json {
            ignoreUnknownKeys = true
        }
        json(json)
    }
}
private val oauthSettings = OAuthServerSettings.OAuth2ServerSettings(
    name = "discord",
    authorizeUrl = "https://discord.com/oauth2/authorize",
    accessTokenUrl = "https://discord.com/api/oauth2/token",
    requestMethod = HttpMethod.Post,
    clientId = Config.DISCORD_CLIENT_ID,
    clientSecret = Config.DISCORD_CLIENT_SECRET,
    defaultScopes = listOf("identify")
)

fun Application.installAuth() {
    install(Sessions) {
        cookie<Session>("AUTH_SESSION")
    }
    authentication {
        oauth(discordAuth) {
            urlProvider = { this@installAuth.buildBotUrl(Route.Auth.Callback()) }
            providerLookup = { oauthSettings }
            client = httpClient
        }

        jwt(jwtAuth) {
            realm = "Soundboard UI Access"
            verifier(jwtVerifier)

            validate { credential ->
                JWTPrincipal(credential.payload)
            }
        }
    }

    routing {
        authenticate(discordAuth) {
            resource<Route.Auth> {
                intercept(ApplicationCallPipeline.Plugins) {
                    val type = call.parameters["type"]?.let(Route.Auth.Type::valueOf) ?: return@intercept
                    call.sessions.set(Session(type))
                }
                get {
                    call.respond("Well this is awkward")
                }
            }

            get<Route.Auth.Callback> {
                val oauth = call.principal<OAuthAccessTokenResponse.OAuth2>()
                    ?: badRequest("Missing auth information")

                val type = call.sessions.get<Session>()?.type ?: badRequest("Missing type")

                val key = httpClient.createJwt(oauth)

                call.respondRedirect(type.redirectTo + key)
            }
        }

        @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
        post<Route.Auth.Token> { (code, state) ->
            val accessToken =
                io.ktor.server.auth.oauth2RequestAccessToken(
                    httpClient,
                    oauthSettings,
                    "",
                    OAuthCallback.TokenSingle(code, state)
                )

            val key = httpClient.createJwt(accessToken)

            call.respond(AuthRefreshResponse(newJwt = key))
        }

        post<Route.Auth.Refresh> {
            val (expiredJwt) = call.receive<AuthRefreshRequest>()
            val refreshToken = runCatching { JWT.decode(expiredJwt).getClaim("refreshToken").asString() }.getOrNull()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val response = httpClient.post("https://discord.com/api/oauth2/token") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(parameters {
                    append(OAuth2RequestParameters.ClientId, Config.DISCORD_CLIENT_ID)
                    append(OAuth2RequestParameters.ClientSecret, Config.DISCORD_CLIENT_SECRET)
                    append(OAuth2RequestParameters.GrantType, "refresh_token")
                    append("refresh_token", refreshToken)
                }.formUrlEncode())
            }
            if (!response.status.isSuccess()) {
                call.respond(HttpStatusCode.Unauthorized)
            } else {
                val oauth = response.body<DiscordAccessTokenResponse>().let {
                    OAuthAccessTokenResponse.OAuth2(it.accessToken, it.tokenType, it.expiresIn, it.refreshToken)
                }
                val jwt = httpClient.createJwt(oauth)
                call.respond(AuthRefreshResponse(newJwt = jwt))
            }
        }
    }
}

fun KtorRoute.authenticated(block: KtorRoute.() -> Unit) = authenticate(
    jwtAuth, build = block
)

val ApplicationCall.isLavalink: Boolean
    get() {
        val audience = principal<JWTPrincipal>()!!.payload.audience?.firstOrNull() ?: return false

        return audience == "lavalink"
    }

val ApplicationCall.userId: Snowflake
    get() {
        val idRaw = principal<JWTPrincipal>()!!.payload.getClaim("userId")
        return snowflake(idRaw.asLong())
    }

private suspend fun HttpClient.createJwt(oauth: OAuthAccessTokenResponse.OAuth2): String {
    val discordUserResponse = get("https://discord.com/api/v${KordConfiguration.REST_VERSION}/users/@me") {
        bearerAuth(oauth.accessToken)
    }

    if (!discordUserResponse.status.isSuccess()) {
        throw BadRequestException("Got unexpected response code: ${discordUserResponse.status}")
    }

    val discordUser = discordUserResponse.body<DiscordUser>()
    return newKey(discordUser.id, oauth.refreshToken!!, oauth.expiresIn.seconds)
}

private fun newKey(userId: Snowflake, refreshToken: String, expiresIn: Duration) = JWT.create()
    .withIssuer(KtorConfig.WEB_SERVER_URL.toString())
    .withClaim("userId", userId.value.toLong())
    .withClaim("refreshToken", refreshToken)
    .withExpiresAt((Clock.System.now() + expiresIn).toJavaInstant())
    .sign(Algorithm.HMAC256(Config.JWT_SECRET))

fun newServiceKey() = JWT.create()
    .withIssuer(KtorConfig.WEB_SERVER_URL.toString())
    .withAudience("lavalink")
    .sign(Algorithm.HMAC256(Config.JWT_SECRET))
