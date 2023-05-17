package dev.schlaubi.tonbrett.bot.server

import dev.schlaubi.tonbrett.bot.io.SoundBoardDatabase
import dev.schlaubi.tonbrett.common.Route.*
import dev.schlaubi.tonbrett.common.Sound
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.intellij.lang.annotations.Language
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.util.KMongoUtil

fun Route.sounds() {
    get<Sounds> { (onlyMine, queryString) ->
        val filter = if (onlyMine) {
            Sound::owner eq call.user.id
        } else {
            null
        }

        val query = if (queryString.isNullOrBlank()) {
            null
        } else {
            KMongoUtil.toBson("{name: /$queryString/}")
        }

        @Language("MongoDB-JSON")
        val result = SoundBoardDatabase.sounds
            .find(and(listOfNotNull(filter, query))).toList()

        call.respond(result)
    }
}
