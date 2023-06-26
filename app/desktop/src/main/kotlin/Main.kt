@file:OptIn(ExperimentalComposeUiApi::class)

package dev.schlaubi.tonbrett.app.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.window.*
import dev.schlaubi.tonbrett.app.ColorScheme
import dev.schlaubi.tonbrett.app.ProvideImageLoader
import dev.schlaubi.tonbrett.app.TonbrettApp
import dev.schlaubi.tonbrett.app.api.*
import dev.schlaubi.tonbrett.app.strings.LocalStrings
import dev.schlaubi.tonbrett.app.title
import dev.schlaubi.tonbrett.client.href
import io.ktor.http.*
import dev.schlaubi.tonbrett.common.Route
import mu.KotlinLogging
import java.net.URI
import java.awt.Window as AWTWindow

private val LOG = KotlinLogging.logger { }

fun main(args: Array<String>) {
    if (windowsAppDataFolder != null) {
        System.setProperty("user.home", windowsAppDataFolder!!)
    }

    val argsString = args.joinToString(" ")
    if (argsString.startsWith("tonbrett://login")) {
        try {
            LOG.info { "Launched App with $argsString saving token now" }
            val token = Url(argsString).parameters["token"]
            saveConfig(Config(token))
        } catch (e: Exception) {
            e.printStackTrace()
            Thread.sleep(50000)
        }
        startApplication()
    } else {
        main(reAuthorize = false) { startApplication() }
    }
}

fun main(reAuthorize: Boolean, onAuth: () -> Unit) {
    val config = getConfig()
    if (reAuthorize && config.sessionToken == null) {
        browseUrl(href(Route.Auth(Route.Auth.Type.PROTOCOL), URLBuilder(getUrl())).build().toURI())
        startAuthorizationServer(reAuthorize, onAuth)
    } else {
        startApplication()
    }
}

fun startApplication() = application {
    val sessionExpired = remember { mutableStateOf(false) }
    var needsUpdate by remember { mutableStateOf(false) }
    val exceptionHandler = ExceptionHandlerFactory {
        LOG.error(it) { "An error occurred" }
        needsUpdate = true
    }
    val strings = LocalStrings.current

    if (needsUpdate) {
        TonbrettWindow {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize().background(ColorScheme.container)
            ) {
                Text(strings.needsUpdate, color = ColorScheme.textColor)
                Button({
                    browseUrl(URI("https://github.com/DRSchlaubi/tonbrett/releases/latest"))
                    exitApplication()
                }) {
                    Icon(Icons.Default.Refresh, null)
                    Text(strings.update)
                }
            }
        }
    } else {
        startActualApplication(exceptionHandler, sessionExpired)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ApplicationScope.startActualApplication(
    exceptionHandler: ExceptionHandlerFactory,
    sessionExpired: MutableState<Boolean>
) {
    CompositionLocalProvider(LocalWindowExceptionHandlerFactory provides exceptionHandler) {
        TonbrettWindow {
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
}

@Composable
private fun ApplicationScope.TonbrettWindow(content: @Composable FrameWindowScope.() -> Unit) = Window(
    onCloseRequest = ::exitApplication,
    title = title,
    icon = BitmapPainter(useResource("logo.png", ::loadImageBitmap)),
    content = content
)

private class ExceptionHandlerFactory(val handler: (Throwable) -> Unit) : WindowExceptionHandlerFactory {
    override fun exceptionHandler(window: AWTWindow): WindowExceptionHandler = ExceptionHandler(window)
    private inner class ExceptionHandler(val window: AWTWindow) : WindowExceptionHandler {
        override fun onException(throwable: Throwable) {
            if (throwable is IllegalArgumentException) {
                handler(throwable)
            } else {
                DefaultWindowExceptionHandlerFactory.exceptionHandler(window).onException(throwable)
            }
        }
    }
}
