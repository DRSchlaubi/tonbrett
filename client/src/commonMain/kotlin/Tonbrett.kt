import dev.schlaubi.tonbrett.common.Route
import dev.schlaubi.tonbrett.common.Sound
import dev.schlaubi.tonbrett.common.User
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*

class Tonbrett {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
        install(WebSockets)
        install(Resources)
    }

    suspend fun getSounds(onlyMine: Boolean = false, query: String? = null): List<Sound> =
        client.get(Route.Sounds(onlyMine, query)).body()

    suspend fun getMe(): User = client.get(Route.Me()).body()

    suspend fun play(soundId: String): Unit = client.post(Route.Sounds.Sound.Play(soundId)).body()
}
