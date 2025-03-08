package dev.schlaubi.tonbrett.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.schlaubi.tonbrett.app.api.IO
import dev.schlaubi.tonbrett.app.api.LocalContext
import dev.schlaubi.tonbrett.app.components.ErrorText
import dev.schlaubi.tonbrett.app.components.SoundList
import dev.schlaubi.tonbrett.client.ReauthorizationRequiredException
import dev.schlaubi.tonbrett.client.Tonbrett
import dev.schlaubi.tonbrett.common.User
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

typealias ErrorReporter = suspend (Exception) -> Unit

private val LOG = KotlinLogging.logger {}

data class TonbrettState(
    val sessionExpired: Boolean = false,
    val crashed: Boolean = false,
    val initialUser: User? = null,
    val loading: Boolean = true
)

class TonbrettViewModel(val scaffoldState: ScaffoldState) : ViewModel() {
    private val _uiState = MutableStateFlow(TonbrettState())
    val uiState = _uiState.asStateFlow()

    fun updateSessionExpired(to: Boolean) {
        _uiState.update { it.copy(sessionExpired = to) }
    }

    suspend fun reportError(exception: Exception) {
        if (exception is ReauthorizationRequiredException) {
            _uiState.update {
                it.copy(
                    sessionExpired = true,
                    crashed = false
                )
            }
        } else if (exception.message.isNullOrBlank()) {
            LOG.error(exception) { "An error happened during a rest request" }
        } else {
            LOG.warn(exception) { "An error occurred" }
            scaffoldState.snackbarHostState.showSnackbar(exception.message!!)
        }
    }

    suspend fun fetchInitialUser(api: Tonbrett) {
        try {
            val initialUser = api.getMe()

            _uiState.update {
                it.copy(
                    initialUser = initialUser,
                    sessionExpired = false,
                    crashed = false
                )
            }
        } catch (e: ClientRequestException) {
            if (e.response.status.value in 400..499) {
                reportError(ReauthorizationRequiredException())
            }
        } catch (e: ReauthorizationRequiredException) {
            reportError(e)
        }
    }

    suspend fun connectToGateway(api: Tonbrett) {
        try {
            api.connect()
        } catch (e: Exception) {
            reportError(e)
        }
        _uiState.update {
            it.copy(crashed = !it.sessionExpired)
        }
    }

    fun restart() {
        _uiState.update {
            it.copy(sessionExpired = false, crashed = false)
        }
    }

    fun updateLoading(to: Boolean) {
        _uiState.update { it.copy(loading = to) }
    }
}

@Composable
fun TonbrettApp(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    model: TonbrettViewModel = viewModel { TonbrettViewModel(scaffoldState) }
) {
    val state by model.uiState.collectAsState()
    val context = LocalContext.current

    val lyricist = rememberStrings()

    LaunchedEffect(context.api) {
        withContext(Dispatchers.IO) {
            if (state.initialUser == null) {
                model.fetchInitialUser(context.api)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ColorScheme.current.container,
        snackbarHost = { SnackbarHost(scaffoldState.snackbarHostState) },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Top)
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(padding)
        ) {
            ProvideImageLoader(newImageLoader(context)) {
                ProvideStrings(lyricist) {
                    val user = state.initialUser
                    if (!state.crashed && !state.sessionExpired) {
                        LaunchedEffect(context.token) {
                            withContext(Dispatchers.IO) {
                                model.connectToGateway(context.api)
                            }
                        }
                        if (state.loading || user == null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.background(ColorScheme.current.container)
                                    .fillMaxSize()
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        if (user != null) {
                            SoundList(model)
                        }
                    } else {
                        if (state.sessionExpired) {
                            CrashErrorScreen(LocalStrings.current.sessionExpiredExplainer) {
                                Button({ context.reAuthorize() }) {
                                    Icon(Icons.Default.Refresh, LocalStrings.current.reAuthorize)
                                    Text(
                                        LocalStrings.current.reAuthorize,
                                        color = ColorScheme.current.textColor
                                    )
                                }
                            }
                        } else if (state.crashed) {
                            CrashErrorScreen(LocalStrings.current.crashedExplainer) {
                                Button({ model.restart() }) {
                                    Icon(Icons.Default.Refresh, LocalStrings.current.reload)
                                    Text(
                                        LocalStrings.current.reload,
                                        color = ColorScheme.current.textColor
                                    )
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
