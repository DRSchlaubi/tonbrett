package dev.schlaubi.tonbrett.bot.server

import dev.schlaubi.tonbrett.bot.config.Config
import dev.schlaubi.tonbrett.bot.io.SoundBoardDatabase
import dev.schlaubi.tonbrett.bot.io.findById
import dev.schlaubi.tonbrett.bot.util.soundNotFound
import dev.schlaubi.tonbrett.common.Route.Sounds
import io.ktor.client.content.*
import io.ktor.http.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.io.path.div

fun Route.files() {
    get<Sounds.Sound.Audio> { (id, contentTypeRaw) ->
        val sound = SoundBoardDatabase.sounds.findById(id) ?: soundNotFound()

        val path = Config.SOUNDS_FOLDER / sound.fileName

        val contentType = contentTypeRaw?.let { ContentType.parse(contentTypeRaw) }
            ?: ContentType.defaultForPath(path)

        val content = LocalFileContent(
            path.toFile(),
            contentType
        )

        call.respond(content)
    }
}
