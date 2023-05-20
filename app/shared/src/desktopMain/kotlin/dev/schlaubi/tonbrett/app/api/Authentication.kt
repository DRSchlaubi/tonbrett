package dev.schlaubi.tonbrett.app.api

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers


@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual val Dispatchers.IO: CoroutineDispatcher
    get() = IO

actual open class AppContext : AppContextBase() {
    override fun getToken(): String = getConfig().sessionToken ?: error("Please sign in")

    actual open fun reAuthorize() = Unit
}
