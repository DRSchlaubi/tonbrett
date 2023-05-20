package dev.schlaubi.tonbrett.app.desktop

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.schlaubi.tonbrett.app.TonbrettApp
import dev.schlaubi.tonbrett.app.api.getConfig
import dev.schlaubi.tonbrett.app.api.getUrl
import dev.schlaubi.tonbrett.app.api.reAuthorize
import dev.schlaubi.tonbrett.app.api.resetApi
import dev.schlaubi.tonbrett.app.title
import dev.schlaubi.tonbrett.common.Route
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.resources.serialization.*
import java.awt.Desktop
import java.net.URI

fun main() = main(reAuthorize = false) { startApplication() }

fun main(reAuthorize: Boolean, onAuth: () -> Unit) {
    val config = getConfig()
    if (reAuthorize || config.sessionToken == null) {
        val urlBuilder = URLBuilder(getUrl())
        href(ResourcesFormat(), Route.Auth(Route.Auth.Type.APP), urlBuilder)
        Desktop.getDesktop().browse(URI(urlBuilder.buildString()))
        startAuthorizationServer(reAuthorize, onAuth)
    } else {
        startApplication()
    }
}

fun startApplication() = application {
    val sessionExpired = remember { mutableStateOf(false) }
    Window(onCloseRequest = ::exitApplication, title = title) {
        reAuthorize = {
            window.isMinimized = true
            main(reAuthorize = true) {
                resetApi()
                window.isMinimized = false
                sessionExpired.value = false
            }
        }
        TonbrettApp(sessionExpired)
    }
}
