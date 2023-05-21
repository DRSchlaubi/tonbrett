import com.liftric.kvault.KVault
import dev.schlaubi.tonbrett.app.api.AppContext
import io.ktor.http.URLBuilder
import dev.schlaubi.tonbrett.app.api.getUrl
import dev.schlaubi.tonbrett.common.Route
import io.ktor.resources.href
import io.ktor.resources.serialization.ResourcesFormat

private val tokenKey = "token"

private val vault = KVault()

fun getTokenOrNull(): String? = vault.string(tokenKey)
fun setToken(token: String) = vault.set(tokenKey, token)

fun getAuthUrl(): String {
    val builder = URLBuilder(getUrl())
    href(ResourcesFormat(), Route.Auth(Route.Auth.Type.MOBILE_APP), builder)
    return builder.buildString()
}

abstract class AppleAppContext : AppContext() {
    override fun getToken(): String = getTokenOrNull() ?: error("Please sign in")

    override fun reAuthorize() {
        vault.deleteObject(tokenKey)
    }
}