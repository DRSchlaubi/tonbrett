package dev.schlaubi.tonbrett.app.api

import android.app.Activity
import coil3.PlatformContext
import com.liftric.kvault.KVault

actual open class AppContext(val currentActivity: Activity) : AppContextBase(), MobileAppContext {
    private val vault: KVault = KVault(currentActivity)
    override var onReAuthorize: () -> Unit = { TODO() }
    actual override val platformContext: PlatformContext = currentActivity
    actual override val isSignedIn: Boolean
        get() = vault.existsObject(tokenKey)

    actual override var token
        get() = getTokenOrNull() ?: error("Please sign in")
        set(value) {
            vault.set(tokenKey, value)
        }

    fun getTokenOrNull() = vault.string(tokenKey)

    /**
     * Initiates authorization flow for the current platform.
     */
    actual fun reAuthorize() {
        vault.deleteObject(tokenKey)
        onReAuthorize()
    }
}
