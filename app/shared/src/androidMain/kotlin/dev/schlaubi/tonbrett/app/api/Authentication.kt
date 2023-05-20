package dev.schlaubi.tonbrett.app.api

import android.content.Context
import com.liftric.kvault.KVault
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

const val tokenKey = "token"

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual val Dispatchers.IO: CoroutineDispatcher
    get() = IO

actual open class AppContext(androidContext: Context) : AppContextBase() {
    val vault = KVault(androidContext)

    fun getTokenOrNull() = vault.string(tokenKey)

    override fun getToken(): String = getTokenOrNull() ?: error("Please sign in")

    actual open fun reAuthorize() = Unit
}
