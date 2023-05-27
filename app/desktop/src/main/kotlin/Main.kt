package dev.schlaubi.tonbrett.app.desktop

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.schlaubi.tonbrett.app.ProvideImageLoader
import dev.schlaubi.tonbrett.app.TonbrettApp
import dev.schlaubi.tonbrett.app.api.AppContext
import dev.schlaubi.tonbrett.app.api.ProvideContext
import dev.schlaubi.tonbrett.app.api.getConfig
import dev.schlaubi.tonbrett.app.api.getUrl
import dev.schlaubi.tonbrett.app.title
import dev.schlaubi.tonbrett.client.href
import dev.schlaubi.tonbrett.common.Route
import io.ktor.http.*
import java.awt.Desktop

fun main() = main(reAuthorize = false) { startApplication() }

fun main(reAuthorize: Boolean, onAuth: () -> Unit) {
    val config = getConfig()
    if (reAuthorize || config.sessionToken == null) {
        Desktop.getDesktop().browse(href(Route.Auth(Route.Auth.Type.APP), URLBuilder(getUrl())).build().toURI())
        startAuthorizationServer(reAuthorize, onAuth)
    } else {
        startApplication()
    }
}

fun startApplication() = application {
    val sessionExpired = remember { mutableStateOf(false) }
    Window(onCloseRequest = ::exitApplication, title = title) {
        val context = remember {
            object : AppContext() {
                override fun reAuthorize() {
                    window.isMinimized = true
                    main(reAuthorize = true) {
                        resetApi()
                        window.isMinimized = false
                        sessionExpired.value = false
                    }
                }
            }
        }
        context.resetApi()
        ProvideContext(context) {
            ProvideImageLoader {
                TonbrettApp(sessionExpired)
            }
        }
    }
}
