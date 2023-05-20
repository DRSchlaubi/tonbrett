package dev.schlaubi.tonbrett.app.web

import androidx.compose.ui.window.CanvasBasedWindow
import dev.schlaubi.tonbrett.app.TonbrettApp
import dev.schlaubi.tonbrett.app.api.tokenKey
import dev.schlaubi.tonbrett.app.title
import dev.schlaubi.tonbrett.common.Route
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.resources.serialization.*
import kotlinx.browser.sessionStorage
import kotlinx.browser.window
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.get
import org.w3c.dom.set

fun main() {
    val url = Url(window.location.href)
    val path = url.pathSegments.filter(String::isNotBlank)
    if (path.take(2) != listOf("soundboard", "ui")) error("Invalid path base")
    if (path.size > 2 && path[2] == "login") {
        sessionStorage[tokenKey] = url.parameters["token"] ?: error("Missing token")
        window.location.href = href(ResourcesFormat(), Route.Ui())
    } else {
        if (sessionStorage[tokenKey] == null) {
            window.location.href = href(ResourcesFormat(), Route.Auth(type = Route.Auth.Type.WEB))
        } else {
            onWasmReady {
                CanvasBasedWindow(title) {
                    TonbrettApp()
                }
            }
        }
    }
}