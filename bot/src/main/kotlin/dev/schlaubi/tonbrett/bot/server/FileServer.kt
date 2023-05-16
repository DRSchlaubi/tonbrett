package dev.schlaubi.tonbrett.bot.server

import dev.schlaubi.tonbrett.bot.config.Config
import dev.schlaubi.tonbrett.bot.io.SoundBoardDatabase
import dev.schlaubi.tonbrett.bot.io.findById
import dev.schlaubi.tonbrett.bot.util.soundNotFound
import dev.schlaubi.tonbrett.common.Route.Sounds
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.io.path.div

fun Route.files() {
    get<Sounds.Sound.Audio> { (id) ->
        val sound = SoundBoardDatabase.sounds.findById(id) ?: soundNotFound()

        val path = Config.SOUNDS_FOLDER / sound.fileName

        call.respondFile(path.toFile())
    }
}
