package dev.schlaubi.tonbrett.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.SnackbarHost
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.schlaubi.tonbrett.app.api.IO
import dev.schlaubi.tonbrett.app.api.LocalContext
import dev.schlaubi.tonbrett.app.components.ErrorText
import dev.schlaubi.tonbrett.app.components.SoundList
import dev.schlaubi.tonbrett.client.ReauthorizationRequiredException
import dev.schlaubi.tonbrett.common.User
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.plugins.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

typealias ErrorReporter = suspend (Exception) -> Unit

private val LOG = KotlinLogging.logger {}

@Composable
fun TonbrettApp(sessionExpiredState: MutableState<Boolean> = remember { mutableStateOf(false) }) {
    val scaffoldState = rememberScaffoldState()
    var sessionExpired by sessionExpiredState
    var crashed by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var initialUser: User? by remember { mutableStateOf(null) }

    val lyricist = rememberStrings()

    suspend fun reportError(exception: Exception) {
        if (exception is ReauthorizationRequiredException) {
            sessionExpired = true
            crashed = false
        } else if (exception.message.isNullOrBlank()) {
            LOG.error(exception) { "An error happened during a rest request" }
        } else {
            LOG.warn(exception) { "An error occurred" }
            scaffoldState.snackbarHostState.showSnackbar(exception.message!!)
        }
    }

    if (initialUser == null) {
        LaunchedEffect(context.api) {
            withContext(Dispatchers.Default) {
                try {
                    initialUser = context.api.getMe()
                } catch (e: ClientRequestException) {
                    if (e.response.status.value in 400..499) {
                        reportError(ReauthorizationRequiredException())
                    }
                } catch (e: ReauthorizationRequiredException) {
                    reportError(e)
                }
            }
        }
    }

    ProvideImageLoader(newImageLoader(context)) {
        ProvideStrings(lyricist) {
            val user = initialUser
            if (!crashed && !sessionExpired && user != null) {
                Scaffold(
                    containerColor = ColorScheme.current.container,
                    snackbarHost = { SnackbarHost(scaffoldState.snackbarHostState) }) { padding ->
                    Column(Modifier.padding(padding)) {
                        SoundList(::reportError, user.voiceState)
                    }
                }

                LaunchedEffect(context.token) {
                    withContext(Dispatchers.IO) {
                        try {
                            context.api.connect()
                        } catch (e: Exception) {
                            reportError(e)
                        }
                        crashed = !sessionExpired
                    }
                }
            } else {
                if (user == null && !sessionExpired) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.background(ColorScheme.current.container)
                            .fillMaxSize()
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.background(ColorScheme.current.container)
                            .fillMaxSize()
                    ) {
                        if (sessionExpired) {
                            CrashErrorScreen(LocalStrings.current.sessionExpiredExplainer) {
                                Button({ context.reAuthorize() }) {
                                    Icon(Icons.Default.Refresh, LocalStrings.current.reAuthorize)
                                    Text(
                                        LocalStrings.current.reAuthorize,
                                        color = ColorScheme.current.textColor
                                    )
                                }
                            }
                        } else if (crashed) {
                            CrashErrorScreen(LocalStrings.current.crashedExplainer) {
                                Button({ crashed = false }) {
                                    Icon(Icons.Default.Refresh, LocalStrings.current.reload)
                                    Text(LocalStrings.current.reload, color = ColorScheme.current.textColor)
                                }
                            }
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
