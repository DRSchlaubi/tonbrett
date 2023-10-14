import com.liftric.kvault.KVault
import dev.schlaubi.tonbrett.app.api.AppContext
import dev.schlaubi.tonbrett.app.api.getUrl
import dev.schlaubi.tonbrett.client.href
import dev.schlaubi.tonbrett.common.Route

private const val tokenKey = "token"

private val vault = KVault()

// Used from swift
@Suppress("unused")
fun setToken(token: String) = vault.set(tokenKey, token)

// Used from swift
@Suppress("unused")
fun getAuthUrl(): String = href(Route.Auth(Route.Auth.Type.MOBILE_APP), getUrl())

abstract class AppleAppContext : AppContext() {
    override fun reAuthorize() {
        vault.deleteObject(tokenKey)
        super.reAuthorize()
    }
}
