package dev.schlaubi.tonbrett.app.api

import coil3.PlatformContext
import io.ktor.http.*
import kotlinx.browser.sessionStorage
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.w3c.dom.get
import org.w3c.dom.set

const val tokenKey = "token"

val appId = window.location.host.substringBefore(".")

actual val Dispatchers.IO: CoroutineDispatcher
    get() = Main // JS Is single threaded so not needed

actual open class AppContext : AppContextBase() {
    actual override val platformContext: PlatformContext = PlatformContext.INSTANCE
    actual override val isSignedIn: Boolean
        get() = sessionStorage[tokenKey] != null

    actual override var token: String
        get() = sessionStorage[tokenKey] ?: error("Please sign in")
        set(value) {
            sessionStorage[tokenKey] = value
        }

    actual open fun reAuthorize() {
        sessionStorage.removeItem(tokenKey)
        window.location.reload()
    }
}

actual fun getUrl() = URLBuilder(window.location.href).apply {
    if (window.location.host.endsWith("discordsays.com")) {
        takeFrom("https://$appId.discordsays.com/.proxy/api/")
    }
}.build()
