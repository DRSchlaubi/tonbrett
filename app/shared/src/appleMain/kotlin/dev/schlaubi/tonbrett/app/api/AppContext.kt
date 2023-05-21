package dev.schlaubi.tonbrett.app.api

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual val Dispatchers.IO: CoroutineDispatcher
    get() = Default

actual open class AppContext : AppContextBase() {
    override fun getToken(): String = throw UnsupportedOperationException("Please implement this")
    actual open fun reAuthorize() = Unit
}