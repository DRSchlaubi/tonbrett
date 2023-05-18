package dev.schlaubi.tonbrett.bot.server

import dev.kord.common.KordConfiguration
import dev.kord.common.entity.DiscordUser
import dev.schlaubi.mikbot.util_plugins.ktor.api.buildBotUrl
import dev.schlaubi.tonbrett.bot.config.Config
import dev.schlaubi.tonbrett.bot.util.badRequest
import dev.schlaubi.tonbrett.common.Route
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import io.ktor.server.routing.Route as KtorRoute

private const val discordAuth = "discord"
private const val sessionAuth = "session"

private val sessionCache = mutableMapOf<String, DiscordUser>()

data class Session(val type: Route.Auth.Type)

private class DiscordPrincipal(val user: DiscordUser) : Principal

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
        register(TokenAuthentication(TokenAuthentication.Config(sessionAuth)))
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

                val id = generateNonce()

                val discordUser =
                    httpClient.get("https://discord.com/api/v${KordConfiguration.REST_VERSION}/users/@me") {
                        bearerAuth(oauth.accessToken)
                    }

                if (!discordUser.status.isSuccess()) {
                    throw BadRequestException("Got unexpected response code: ${discordUser.status}")
                }
                sessionCache[id] = discordUser.body()

                call.respondRedirect(type.redirectTo + id)
            }
        }
    }
}

fun KtorRoute.authenticated(block: KtorRoute.() -> Unit) = authenticate(
    sessionAuth, build = block
)

private class TokenAuthentication(config: Config) : AuthenticationProvider(config) {
    class Config(name: String) : AuthenticationProvider.Config(name)

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val authToken = context.call.request.parseAuthorizationHeader() ?: run {
            context.error("MISSING_TOKEN", AuthenticationFailedCause.NoCredentials)
            return
        }
        if (authToken !is HttpAuthHeader.Single || authToken.authScheme != "Bearer" || authToken.blob !in sessionCache) {
            context.error("WRONG_TOKEN", AuthenticationFailedCause.InvalidCredentials)
            return
        }
        val session = sessionCache[authToken.blob]!!

        context.principal(DiscordPrincipal(session))
    }

}

fun findSession(id: String) = sessionCache[id]

val ApplicationCall.user: DiscordUser
    get() {
        if (authentication.allFailures.isNotEmpty()) {
            throw OAuth2Exception.MissingAccessToken()
        }

        return principal<DiscordPrincipal>()!!.user
    }
