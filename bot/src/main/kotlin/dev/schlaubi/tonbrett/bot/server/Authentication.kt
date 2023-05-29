@file:OptIn(InternalAPI::class)

package dev.schlaubi.tonbrett.bot.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import dev.kord.common.KordConfiguration
import dev.kord.common.entity.DiscordUser
import dev.kord.common.entity.Snowflake as snowflake
import dev.schlaubi.mikbot.plugin.api.InternalAPI
import dev.schlaubi.mikbot.util_plugins.ktor.api.buildBotUrl
import dev.schlaubi.tonbrett.bot.config.Config
import dev.schlaubi.tonbrett.bot.util.badRequest
import dev.schlaubi.tonbrett.common.Route
import dev.schlaubi.tonbrett.common.Snowflake
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.days
import dev.schlaubi.mikbot.util_plugins.ktor.api.Config as KtorConfig
import io.ktor.server.routing.Route as KtorRoute

private const val discordAuth = "discord"
private const val jwtAuth = "jwt"

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

fun Application.installAuth() {
    install(Sessions) {
        cookie<Session>("AUTH_SESSION")
    }
    authentication {
        oauth(discordAuth) {
            urlProvider = { this@installAuth.buildBotUrl(Route.Auth.Callback()) }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "discord",
                    authorizeUrl = "https://discord.com/oauth2/authorize",
                    accessTokenUrl = "https://discord.com/api/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = Config.DISCORD_CLIENT_ID,
                    clientSecret = Config.DISCORD_CLIENT_SECRET,
                    defaultScopes = listOf("identify")
                )
            }
            client = httpClient
        }

        jwt(jwtAuth) {
            realm = "Soundboard UI Access"
            verifier(jwtVerifier)

            validate {credential ->
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


                val discordUserResponse =
                    httpClient.get("https://discord.com/api/v${KordConfiguration.REST_VERSION}/users/@me") {
                        bearerAuth(oauth.accessToken)
                    }

                if (!discordUserResponse.status.isSuccess()) {
                    throw BadRequestException("Got unexpected response code: ${discordUserResponse.status}")
                }

                val discordUser  =discordUserResponse.body<DiscordUser>()
                val key = newKey(discordUser.id)

                call.respondRedirect(type.redirectTo + key)
            }
        }
    }
}

fun KtorRoute.authenticated(block: KtorRoute.() -> Unit) = authenticate(
    jwtAuth, build = block
)

val ApplicationCall.userId: Snowflake
    get() {
        val idRaw = principal<JWTPrincipal>()!!.payload.getClaim("userId")
        return snowflake(idRaw.asLong())
    }

private fun newKey(userId: Snowflake) = JWT.create()
    .withIssuer(KtorConfig.WEB_SERVER_URL.toString())
    .withClaim("userId", userId.value.toLong())
    .withExpiresAt((Clock.System.now() + 7.days).toJavaInstant())
    .sign(Algorithm.HMAC256(Config.JWT_SECRET))
