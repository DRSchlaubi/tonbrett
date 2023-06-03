package dev.schlaubi.tonbrett.app.web

import androidx.compose.ui.window.CanvasBasedWindow
import dev.schlaubi.tonbrett.app.TonbrettApp
import dev.schlaubi.tonbrett.app.api.AppContext
import dev.schlaubi.tonbrett.app.api.ProvideContext
import dev.schlaubi.tonbrett.app.api.tokenKey
import dev.schlaubi.tonbrett.app.title
import dev.schlaubi.tonbrett.client.href
import dev.schlaubi.tonbrett.common.Route
import io.ktor.http.*
import kotlinx.browser.sessionStorage
import kotlinx.browser.window
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.get
import org.w3c.dom.set

private val context = AppContext()

fun main() {
    val url = Url(window.location.href)
    val path = url.pathSegments.filter(String::isNotBlank)
    if (path.first() != "soundboard" || path.size < 2) error("Invalid path base")
    if (path[1] == "ui") {
        if (path.size > 2 && path[2] == "login") {
            sessionStorage[tokenKey] = url.parameters["token"] ?: error("Missing token")
            window.location.href = href(Route.Ui())
        } else {
            if (sessionStorage[tokenKey] == null) {
                window.location.href = href(Route.Auth(type = Route.Auth.Type.WEB))
            } else {
                onWasmReady {
                    context.resetApi()
                    CanvasBasedWindow(title) {
                        ProvideContext(context) {
                            TonbrettApp()
                        }
                    }
                }
            }
        }
    } else if (path.subList(1, 3) == listOf("deeplink", "login")){
        onWasmReady {
            CanvasBasedWindow(title) {
                AuthorizationScreen(url.parameters["cli"]?.toBoolean() ?: false)
            }
        }
    }
}
