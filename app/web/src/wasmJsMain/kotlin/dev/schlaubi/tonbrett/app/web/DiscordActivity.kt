package dev.schlaubi.tonbrett.app.web

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.CanvasBasedWindow
import dev.schlaubi.tonbrett.app.TonbrettApp
import dev.schlaubi.tonbrett.app.api.AppContext
import dev.schlaubi.tonbrett.app.api.ProvideContext
import dev.schlaubi.tonbrett.app.api.appId
import dev.schlaubi.tonbrett.app.api.getUrl
import dev.schlaubi.tonbrett.app.title
import dev.schlaubi.tonbrett.common.AuthRefreshResponse
import dev.schlaubi.tonbrett.common.Route
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.coroutines.await

private val apiClient by lazy {
    HttpClient {
        install(Resources)
        install(ContentNegotiation) {
            json()
        }

        defaultRequest {
            url.takeFrom(getUrl())
        }
    }
}

val discordSdk by lazy { DiscordSDK(appId) }

private enum class State {
    INITIALIZING,
    AUTHORIZING,
    RUNNING
}

private val context = AppContext()

fun discordActivity() {
    try {
        CanvasBasedWindow(title) {
            var state by remember { mutableStateOf(State.INITIALIZING) }

            LaunchedEffect(Unit) {
                discordSdk.ready().await<JsAny?>()

                state = if (context.isSignedIn) State.RUNNING else State.AUTHORIZING
                println("Switching state to $state")
            }

            if (state == State.AUTHORIZING) {
                LaunchedEffect(Unit) {
                    val (token) = requestToken()
                    context.token = token

                    state = State.RUNNING
                    println("Switching state to $state")
                }
            } else if (state == State.RUNNING) {
                ProvideContext(context) {
                    context.resetApi()
                    TonbrettApp()
                }
            }
        }
    } catch (e: Throwable) {
        println("Got an error: ${e.message}")
        e.printStackTrace()
    }
}

private suspend fun requestToken(): AuthRefreshResponse {
    val state = generateNonce()

    val response = discordSdk.authorize(appId, state).await<AuthorizeResponse>()
    return apiClient.exchangeToken(response.code.toString(), state)
}

/**
 * Exchanges [code] for an access token.
 */
private suspend fun HttpClient.exchangeToken(code: String, state: String): AuthRefreshResponse =
    post(Route.Auth.Token(code, state)).body()
