package dev.schlaubi.tonbrett.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.SnackbarHost
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.schlaubi.tonbrett.app.api.api
import dev.schlaubi.tonbrett.app.components.ErrorText
import dev.schlaubi.tonbrett.app.components.SoundList
import dev.schlaubi.tonbrett.app.strings.LocalStrings
import dev.schlaubi.tonbrett.app.strings.ProvideStrings
import dev.schlaubi.tonbrett.app.strings.rememberStrings
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import mu.KotlinLogging

typealias ErrorReporter = suspend (ClientRequestException) -> Unit

private val LOG = KotlinLogging.logger {}

@Composable
fun TonbrettApp() {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    var crashed by remember { mutableStateOf(false) }

    val lyricist = rememberStrings()
    suspend fun reportError(exception: ClientRequestException) {
        val errorMessage = exception.response.bodyAsText().ifBlank {
            "An unknown error occurred check browser console for more"
        }

        if (exception.message.isBlank()) {
            LOG.error(exception) { "An error happened during a rest request" }
        }

        scaffoldState.snackbarHostState.showSnackbar(errorMessage)
    }

    ProvideStrings(lyricist) {
        if (!crashed) {
            Scaffold(
                containerColor = ColorScheme.container,
                snackbarHost = { SnackbarHost(scaffoldState.snackbarHostState) }) {
                SoundList(::reportError)
            }

            DisposableEffect(Unit) {
                scope.launch {
                    api.connect()
                    crashed = true
                }
                onDispose { scope.cancel() }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.background(ColorScheme.container)
                    .fillMaxSize()
            ) {
                Row {
                    ErrorText(LocalStrings.current.crashedExplainer)
                }

                Row {
                    Button({ crashed = false }) {
                        Icon(Icons.Default.Refresh, LocalStrings.current.reload)
                        Text(LocalStrings.current.reload, color = ColorScheme.textColor)
                    }
                }
            }
        }
    }
}
