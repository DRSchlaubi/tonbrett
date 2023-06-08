package dev.schlaubi.tonbrett.app.api

import androidx.compose.runtime.*
import dev.schlaubi.tonbrett.client.Tonbrett
import io.ktor.http.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

expect fun getUrl(): Url

abstract class AppContextBase {
    abstract fun getToken(): String

    private val apiState = mutableStateOf<Tonbrett?>(null)

    val api: Tonbrett get() = apiState.value ?: error("App not initialized")

    fun resetApi() {
        apiState.value = Tonbrett(getToken(), getUrl())
    }
}

expect open class AppContext : AppContextBase {
    fun reAuthorize()
}

@Suppress("KotlinRedundantDiagnosticSuppress", "EXTENSION_SHADOWED_BY_MEMBER")
expect val Dispatchers.IO: CoroutineDispatcher

val LocalContext: ProvidableCompositionLocal<AppContext> =
    compositionLocalOf { error("App context does not have default") }

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
