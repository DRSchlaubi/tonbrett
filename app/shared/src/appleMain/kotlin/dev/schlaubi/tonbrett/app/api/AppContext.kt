package dev.schlaubi.tonbrett.app.api

import com.liftric.kvault.KVault
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.UIKit.UIViewController

actual val Dispatchers.IO: CoroutineDispatcher
    get() = IO

actual open class AppContext : AppContextBase(), MobileAppContext {
    private val vault: KVault = KVault()
    override var onReAuthorize: () -> Unit = { TODO() }
    actual override val isSignedIn: Boolean
        get() = vault.existsObject(tokenKey)

    actual override var token
        get() = getTokenOrNull() ?: error("Please sign in")
        set(value) {
            vault.set(tokenKey, value)
        }

    open fun present(viewController: UIViewController): Unit = TODO()

    fun getTokenOrNull() = vault.string(tokenKey)

    /**
     * Initiates authorization flow for the current platform.
     */
    actual fun reAuthorize() {
        vault.deleteObject(tokenKey)
        onReAuthorize()
    }
}