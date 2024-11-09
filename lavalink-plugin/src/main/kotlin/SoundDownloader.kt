package dev.schlaubi.tonbrett.lavalink

import dev.schlaubi.tonbrett.client.Tonbrett
import dev.schlaubi.tonbrett.common.Route
import dev.schlaubi.tonbrett.common.Sound
import dev.schlaubi.tonbrett.common.SoundGroup
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.Url
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

private val LOG = KotlinLogging.logger { }

@Component
class SoundDownloader(config: Config) {
    private val api: Tonbrett
    private val client = HttpClient()
    companion object {
        val cache = Path("sounds")
    }

    init {
        val token = config.token ?: error("Please provide plugins.tonbrett.token")
        val host = config.host ?: error("Please provide plugins.tonbrett.host")

        if (!cache.exists()) {
            cache.createDirectories()
        }

        api = Tonbrett(token, Url(host), isService = true)
    }

    fun deleteSound(id: String) {
        val file = cache / id
        if (file.exists()) {
            file.deleteIfExists()
        }
    }

    suspend fun downloadSound(sound: Sound) {
        val destination = cache / sound.id.toString()
        if (destination.exists()) return

        val url = if (sound.isDiscord) {
            "https://cdn.discordapp.com/soundboard-sounds/${sound.id}"
        } else {
            api.href(Route.Sounds.Sound.Audio(sound.id.toString()))
        }

        LOG.debug { "Downloading sound: ${sound.id} from $url" }

        val body = client.get(url).bodyAsChannel()

        body.copyAndClose(destination.toFile().writeChannel())

    }

    suspend fun syncSounds() {
        val (sounds, deletions) = coroutineScope {
            val sounds = api.getSounds()
                .asSequence()
                .flatMap(SoundGroup::sounds)
                .onEach { launch { downloadSound(it) } }
                .map(Sound::id)
                .map(Any::toString)
                .toList()

            val deletions = cache.listDirectoryEntries()
                .asSequence()
                .filter { it.fileName.toString() !in sounds }
                .onEach { it.deleteIfExists() }
                .count()

            sounds to deletions
        }

        LOG.info { "Successfully synced ${sounds.size} sounds and deleted $deletions sounds" }
    }
}