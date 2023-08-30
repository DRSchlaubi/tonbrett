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
import cafe.adriel.lyricist.LocalStrings
import dev.schlaubi.tonbrett.app.ColorScheme
import dev.schlaubi.tonbrett.app.ProvideImageLoader
import dev.schlaubi.tonbrett.app.TonbrettApp
import dev.schlaubi.tonbrett.app.api.ProvideContext
import dev.schlaubi.tonbrett.app.api.getUrl
import dev.schlaubi.tonbrett.app.title
import dev.schlaubi.tonbrett.client.href
import dev.schlaubi.tonbrett.common.Route
import io.ktor.http.*
import io.ktor.serialization.ContentConvertException
import mu.KotlinLogging
import java.net.URI
import kotlin.system.exitProcess
import java.awt.Window as AWTWindow

private val LOG = KotlinLogging.logger { }

fun main(args: Array<String>) {
    val uwp = windowsAppDataFolder != null
    if (uwp) {
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
        startApplication(uwp)
    } else {
        main(reAuthorize = false, uwp = uwp) { startApplication(uwp) }
    }
}

fun main(reAuthorize: Boolean, uwp: Boolean = false, onAuth: () -> Unit) {
    val config = getConfig()
    if (reAuthorize || config.sessionToken == null) {
        val protocol = if (uwp) {
            Route.Auth.Type.PROTOCOL
        } else {
            Route.Auth.Type.APP
        }
        launchUri(href(Route.Auth(protocol), URLBuilder(getUrl())).build().toURI())
        if (!uwp) {
            startAuthorizationServer(reAuthorize, onAuth)
        } else {
            exitProcess(0)
        }
    } else {
        startApplication(uwp)
    }
}

fun startApplication(uwp: Boolean) = application {
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
                    launchUri(URI("https://github.com/DRSchlaubi/tonbrett/releases/latest"))
                    exitApplication()
                }) {
                    Icon(Icons.Default.Refresh, null)
                    Text(strings.update)
                }
            }
        }
    } else {
        startActualApplication(uwp, exceptionHandler, sessionExpired)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ApplicationScope.startActualApplication(
    uwp: Boolean,
    exceptionHandler: ExceptionHandlerFactory, sessionExpired: MutableState<Boolean>
) {
    CompositionLocalProvider(LocalWindowExceptionHandlerFactory provides exceptionHandler) {
        TonbrettWindow {
            val context = remember {
                object : ConfigBasedAppContext() {
                    override fun reAuthorize() {
                        window.isMinimized = true
                        main(reAuthorize = true, uwp = uwp) {
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
        @OptIn(ExperimentalComposeUiApi::class)
        override fun onException(throwable: Throwable) {
            if (throwable is IllegalArgumentException || throwable is ContentConvertException) {
                handler(throwable)
            } else {
                DefaultWindowExceptionHandlerFactory.exceptionHandler(window).onException(throwable)
            }
        }
    }
}
