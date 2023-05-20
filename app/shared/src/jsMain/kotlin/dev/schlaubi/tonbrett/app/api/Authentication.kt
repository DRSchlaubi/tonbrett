package dev.schlaubi.tonbrett.app.api

import io.ktor.http.*
import kotlinx.browser.sessionStorage
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.w3c.dom.get

const val tokenKey = "token"

actual val Dispatchers.IO: CoroutineDispatcher
    get() = Main

actual fun getToken() = sessionStorage[tokenKey] ?: error("Please sign in")

actual fun getUrl() = URLBuilder(window.location.href).apply {
    pathSegments = emptyList()
}.build()

actual fun reAuthorize() {
    sessionStorage.removeItem(tokenKey)
    window.location.reload()
}
