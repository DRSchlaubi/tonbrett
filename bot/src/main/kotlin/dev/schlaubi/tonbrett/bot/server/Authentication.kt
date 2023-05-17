package dev.schlaubi.tonbrett.bot.server

import dev.kord.common.KordConfiguration
import dev.kord.common.entity.DiscordUser
import dev.schlaubi.mikbot.util_plugins.ktor.api.buildBotUrl
import dev.schlaubi.tonbrett.bot.config.Config
import dev.schlaubi.tonbrett.common.Route
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
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

data class Session(val id: String) : Principal

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
        cookie<Session>("SESSION")
    }

    authentication {
        session<Session>(sessionAuth) {
            validate { it.takeIf { it.id in sessionCache } }
        }
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
            get<Route.Auth> {
                call.respond("Well this is awkward")
            }

            get<Route.Auth.Callback> {
                val oauth = call.principal<OAuthAccessTokenResponse.OAuth2>()
                    ?: throw BadRequestException("Missing auth information")

                val id = generateNonce()

                val discordUser = httpClient.get("https://discord.com/api/v${KordConfiguration.REST_VERSION}/users/@me") {
                    bearerAuth(oauth.accessToken)
                }

                if (!discordUser.status.isSuccess()) {
                    throw BadRequestException("Got unexpected response code: ${discordUser.status}")
                }
                sessionCache[id] = discordUser.body()

                call.sessions.set(Session(id))

                call.respondRedirect("/home")
            }
        }
    }
}

fun KtorRoute.authenticated(block: KtorRoute.() -> Unit) = authenticate(
    sessionAuth, build = block)

val ApplicationCall.user: DiscordUser
    get() {
        val session = sessions.get<Session>() ?:
            throw OAuth2Exception.MissingAccessToken()

        val user = sessionCache[session.id] ?:
            throw OAuth2Exception.MissingAccessToken()

        return user
    }
