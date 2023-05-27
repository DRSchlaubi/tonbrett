package dev.schlaubi.tonbrett.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.SnackbarHost
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.schlaubi.tonbrett.app.api.IO
import dev.schlaubi.tonbrett.app.api.LocalContext
import dev.schlaubi.tonbrett.app.components.ErrorText
import dev.schlaubi.tonbrett.app.components.SoundList
import dev.schlaubi.tonbrett.app.strings.LocalStrings
import dev.schlaubi.tonbrett.app.strings.ProvideStrings
import dev.schlaubi.tonbrett.app.strings.rememberStrings
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging

typealias ErrorReporter = suspend (ClientRequestException) -> Unit

private val LOG = KotlinLogging.logger {}

@Composable
fun TonbrettApp(sessionExpiredState: MutableState<Boolean> = remember { mutableStateOf(false) }) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    var sessionExpired by sessionExpiredState
    var crashed by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val lyricist = rememberStrings()
    fun reportError(exception: ClientRequestException) {
        if (exception.response.status == HttpStatusCode.Unauthorized) {
            sessionExpired = true
            crashed = false
        } else if (exception.message.isBlank()) {
            LOG.error(exception) { "An error happened during a rest request" }
        }
    }

    ProvideStrings(lyricist) {
        if (!crashed && !sessionExpired) {
            Scaffold(
                containerColor = ColorScheme.container,
                snackbarHost = { SnackbarHost(scaffoldState.snackbarHostState) }) {
                SoundList(::reportError)
            }

            LaunchedEffect(context.getToken()) {
                withContext(Dispatchers.IO) {
                    try {
                        context.api.connect()
                    } catch (e: ClientRequestException) {
                        reportError(e)
                    }
                    crashed = !sessionExpired
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.background(ColorScheme.container)
                    .fillMaxSize()
            ) {
                if (sessionExpired) {
                    CrashErrorScreen(LocalStrings.current.sessionExpiredExplainer) {
                        Button({ context.reAuthorize() }) {
                            Icon(Icons.Default.Refresh, LocalStrings.current.reAuthorize)
                            Text(LocalStrings.current.reAuthorize, color = ColorScheme.textColor)
                        }
                    }
                } else {
                    CrashErrorScreen(LocalStrings.current.crashedExplainer) {
                        Button({ crashed = false }) {
                            Icon(Icons.Default.Refresh, LocalStrings.current.reload)
                            Text(LocalStrings.current.reload, color = ColorScheme.textColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CrashErrorScreen(text: String, button: @Composable RowScope.() -> Unit) {
    Row {
        ErrorText(text)
    }
    Row {
        button()
    }
}
