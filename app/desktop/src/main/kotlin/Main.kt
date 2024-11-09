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
import androidx.compose.ui.window.*
import dev.schlaubi.tonbrett.app.LocalStrings
import dev.schlaubi.tonbrett.app.ColorScheme
import dev.schlaubi.tonbrett.app.ProvideLocalWindow
import dev.schlaubi.tonbrett.app.TonbrettApp
import dev.schlaubi.tonbrett.app.api.ProvideContext
import dev.schlaubi.tonbrett.app.desktop.Platform.launchUri
import dev.schlaubi.tonbrett.app.desktop.Platform.start
import dev.schlaubi.tonbrett.app.title
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.serialization.*
import org.jetbrains.compose.resources.imageResource
import java.net.URI
import java.awt.Window as AWTWindow

private val LOG = KotlinLogging.logger { }

fun main(array: Array<String>) = start(array)

fun startApplication(forAuth: Boolean = false) = application {
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
                modifier = Modifier.fillMaxSize().background(ColorScheme.current.container)
            ) {
                Text(strings.needsUpdate, color = ColorScheme.current.textColor)
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
        startActualApplication(forAuth, exceptionHandler, sessionExpired)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ApplicationScope.startActualApplication(
    forAuth: Boolean,
    exceptionHandler: ExceptionHandlerFactory, sessionExpired: MutableState<Boolean>
) {
    var needsAuth by remember { mutableStateOf(forAuth) }
    var firstAuth by remember { mutableStateOf(true) }
    CompositionLocalProvider(LocalWindowExceptionHandlerFactory provides exceptionHandler) {
        TonbrettWindow {
            val context = remember {
                object : TokenStorageAppContext() {
                    override fun reAuthorize() {
                        sessionExpired.value = false
                        firstAuth = false
                        needsAuth = true
                    }
                }
            }

            if (needsAuth) {
                AuthorizationScreen(!firstAuth) { needsAuth = false }
            } else {
                context.resetApi()
                ProvideContext(context) {
                    TonbrettApp()
                }
            }
        }
    }
}

@Composable
fun ApplicationScope.TonbrettWindow(content: @Composable FrameWindowScope.() -> Unit) = Window(
    onCloseRequest = ::exitApplication,
    title = title,
    icon = BitmapPainter(imageResource(Res.drawable.logo))
) {
    ProvideLocalWindow(window) {
        content()
    }
}

private class ExceptionHandlerFactory(val handler: (Throwable) -> Unit) : WindowExceptionHandlerFactory {
    override fun exceptionHandler(window: AWTWindow): WindowExceptionHandler = ExceptionHandler(window)
    private inner class ExceptionHandler(val window: AWTWindow) : WindowExceptionHandler {
        @OptIn(ExperimentalComposeUiApi::class)
        override fun onException(throwable: Throwable) {
            if (throwable is IllegalArgumentException || throwable is ContentConvertException) {
                throwable.printStackTrace()
                handler(throwable)
            } else {
                DefaultWindowExceptionHandlerFactory.exceptionHandler(window).onException(throwable)
            }
        }
    }
}
