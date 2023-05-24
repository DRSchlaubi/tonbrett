package dev.schlaubi.tonbrett.bot.server

import com.kotlindiscord.kord.extensions.koin.KordExContext
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.Kord
import dev.schlaubi.lavakord.kord.connectAudio
import dev.schlaubi.tonbrett.bot.core.soundPlayer
import dev.schlaubi.tonbrett.bot.core.voiceState
import dev.schlaubi.tonbrett.bot.io.SoundBoardDatabase
import dev.schlaubi.tonbrett.bot.io.findAllTags
import dev.schlaubi.tonbrett.bot.io.findById
import dev.schlaubi.tonbrett.bot.io.search
import dev.schlaubi.tonbrett.bot.util.badRequest
import dev.schlaubi.tonbrett.bot.util.soundNotFound
import dev.schlaubi.tonbrett.bot.util.translate
import dev.schlaubi.tonbrett.common.Route.Sounds
import dev.schlaubi.tonbrett.common.Route.Tags
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import kotlinx.coroutines.flow.toList

@OptIn(KordUnsafe::class, KordExperimental::class)
fun Route.sounds() {
    val kord by KordExContext.get().inject<Kord>()

    get<Tags> { (query, limit) ->
        call.respond(SoundBoardDatabase.sounds.findAllTags(query, limit).toList())
    }

    get<Sounds> { (onlyMine, queryString) ->
        call.respond(SoundBoardDatabase.sounds.search(queryString, onlyMine, call.user.id).toList())
    }

    authenticated {
        post<Sounds.Sound.Play> { (soundId) ->
            val sound = SoundBoardDatabase.sounds.findById(soundId) ?: soundNotFound()
            val voiceState = call.user.voiceState
                ?: badRequest(call.translate("rest.errors.not_connected_to_vc"))
            val player = kord.unsafe.guild(voiceState.guildId).soundPlayer

            @Suppress("INVISIBLE_MEMBER", "EQUALITY_NOT_APPLICABLE")
            if (player.channelId == null) {
                player.player.link.connectAudio(voiceState.channelId)
            } else if (player.channelId != voiceState.channelId) {
                badRequest(call.translate("rest.errors.vc_mismatch"))
            }

            player.playSound(sound)
            call.respond(HttpStatusCode.Accepted)
        }
    }
}
