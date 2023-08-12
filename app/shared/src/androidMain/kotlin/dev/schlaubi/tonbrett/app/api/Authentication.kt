package dev.schlaubi.tonbrett.app.api

import android.content.Context
import com.liftric.kvault.KVault

const val tokenKey = "token"

actual open class AppContext(val androidContext: Context) : AppContextBase() {
    val vault = KVault(androidContext)

    fun getTokenOrNull() = vault.string(tokenKey)

    override var token
        get() = getTokenOrNull() ?: error("Please sign in")
        set(value) {
            vault.set(tokenKey, value)
        }

    actual open fun reAuthorize() = Unit
}
