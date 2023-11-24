package dev.schlaubi.tonbrett.app.api

import androidx.compose.runtime.*
import dev.schlaubi.tonbrett.client.Tonbrett
import io.ktor.http.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Returns the API base url.
 */
expect fun getUrl(): Url

abstract class ApiStateHolder {
    /**
     * A mutable API token.
     */
    abstract var token: String
    private val apiState = mutableStateOf<Tonbrett?>(null)

    /**
     * The current API wrapper instance.
     */
    val api: Tonbrett get() = apiState.value ?: error("App not initialized")

    /**
     * Resets [api] to a fresh instance.
     */
    fun resetApi() {
        apiState.value = Tonbrett(token, getUrl(), onTokenRefresh = { token = it })
    }
}

/**
 * This is the base class of [AppContext] so we can have defaults.
 */
abstract class AppContextBase : ApiStateHolder() {
    abstract override var token: String
    abstract val isSignedIn: Boolean
}

/**
 * Context used for piping platform contexts into a multiplatform app.
 */
expect open class AppContext : AppContextBase {
    override var token: String
    override val isSignedIn: Boolean

    /**
     * Initiates authorization flow for the current platform.
     */
    fun reAuthorize()
}

/**
 * The dispatcher that should be used for I/O on this platform.
 */
@Suppress("KotlinRedundantDiagnosticSuppress", "EXTENSION_SHADOWED_BY_MEMBER")
expect val Dispatchers.IO: CoroutineDispatcher

/**
 * A composition local for the current context.
 */
val LocalContext: ProvidableCompositionLocal<AppContext> =
    staticCompositionLocalOf { error("App context does not have default") }

/**
 * Provides [context] to [content].
 */
@Composable
fun ProvideContext(
    context: AppContext,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalContext provides context,
        content = content
    )
}
