import com.liftric.kvault.KVault
import dev.schlaubi.tonbrett.app.api.AppContext
import dev.schlaubi.tonbrett.app.api.getUrl
import dev.schlaubi.tonbrett.client.href
import dev.schlaubi.tonbrett.common.Route

private const val tokenKey = "token"

private val vault = KVault()

fun getTokenOrNull(): String? = vault.string(tokenKey)

// Used from swift
@Suppress("unused")
fun setToken(token: String) = vault.set(tokenKey, token)

// Used from swift
@Suppress("unused")
fun getAuthUrl(): String = href(Route.Auth(Route.Auth.Type.MOBILE_APP), getUrl())

abstract class AppleAppContext : AppContext() {
    override var token: String
        get() = getTokenOrNull() ?: error("Please sign in")
        set(value) {
            setToken(value)
        }

    override fun reAuthorize() {
        vault.deleteObject(tokenKey)
    }
}
