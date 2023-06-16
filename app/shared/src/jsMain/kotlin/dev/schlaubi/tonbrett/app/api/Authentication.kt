package dev.schlaubi.tonbrett.app.api

import io.ktor.http.*
import kotlinx.browser.sessionStorage
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.w3c.dom.get
import org.w3c.dom.set

const val tokenKey = "token"

actual val Dispatchers.IO: CoroutineDispatcher
    get() = Main

actual open class AppContext : AppContextBase() {
    override var token: String
        get() = sessionStorage[tokenKey] ?: error("Please sign in")
        set(value) {
            sessionStorage[tokenKey] = value
        }

    actual fun reAuthorize() {
        sessionStorage.removeItem(tokenKey)
        window.location.reload()
    }
}

actual fun getUrl() = URLBuilder(window.location.href).apply {
    pathSegments = emptyList()
}.build()
