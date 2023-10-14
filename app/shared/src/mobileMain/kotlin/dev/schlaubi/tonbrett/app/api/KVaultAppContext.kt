package dev.schlaubi.tonbrett.app.api

import com.liftric.kvault.KVault


const val tokenKey = "token"

actual abstract class AppContextBase : ApiStateHolder() {
    abstract val vault: KVault

    protected var onReAuthorize: () -> Unit = {}
        private set
    actual open val isSignedIn: Boolean
        get() = vault.existsObject(tokenKey)

    fun getTokenOrNull() = vault.string(tokenKey)

    override var token
        get() = getTokenOrNull() ?: error("Please sign in")
        set(value) {
            vault.set(tokenKey, value)
        }

    fun withReAuthroize(block: () -> Unit) {
        onReAuthorize = block
    }
}