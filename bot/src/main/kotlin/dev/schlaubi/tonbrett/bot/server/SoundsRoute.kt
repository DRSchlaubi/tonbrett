package dev.schlaubi.tonbrett.bot.server

import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.Kord
import dev.kordex.core.koin.KordExContext
import dev.schlaubi.lavakord.kord.connectAudio
import dev.schlaubi.tonbrett.bot.core.soundPlayer
import dev.schlaubi.tonbrett.bot.core.voiceState
import dev.schlaubi.tonbrett.bot.io.SoundBoardDatabase
import dev.schlaubi.tonbrett.bot.io.findAllTags
import dev.schlaubi.tonbrett.bot.io.findById
import dev.schlaubi.tonbrett.bot.io.searchGrouped
import dev.schlaubi.tonbrett.bot.util.badRequest
import dev.schlaubi.tonbrett.bot.util.soundNotFound
import dev.schlaubi.tonbrett.bot.util.translate
import dev.schlaubi.tonbrett.common.Route.*
import dev.schlaubi.tonbrett.common.util.convertForNonJvmPlatforms
import io.ktor.http.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlin.time.Duration.Companion.milliseconds

@OptIn(KordUnsafe::class, KordExperimental::class)
fun Route.sounds() {
    val kord by lazy { KordExContext.get().get<Kord>() }

    post<StopPlayer> {
        val guild = call.userId.voiceState?.guildId ?: badRequest("Not connected to voice channel")
        val player = kord.unsafe.guild(guild).soundPlayer
        if (!player.locked) badRequest("Player not playing")
        player.stop()
        call.respond(HttpStatusCode.Accepted)
    }

    get<Tags> { (query, limit) ->
        call.respond(SoundBoardDatabase.sounds.findAllTags(query, limit).toList())
    }

    get<Sounds.ListSounds> { (onlyMine, queryString, useUnicode) ->
        call.respond(
            SoundBoardDatabase.sounds.searchGrouped(queryString, onlyMine, call.userId).toList()
                .convertForNonJvmPlatforms(!useUnicode)
        )
    }

    post<Sounds.Sound.Play> { (soundId) ->
        val sound = SoundBoardDatabase.sounds.findById(soundId) ?: soundNotFound()
        val user = call.userId
        val voiceState = user.voiceState
            ?: badRequest(call.translate("rest.errors.not_connected_to_vc"))
        val player = kord.unsafe.guild(voiceState.guildId).soundPlayer
        if (player.locked && player.currentUser != user) {
            badRequest("You are not permitted to skip a track")
        }
        @Suppress("INVISIBLE_MEMBER", "EQUALITY_NOT_APPLICABLE")
        if (player.channelId == null) {
            player.player.link.connectAudio(voiceState.channelId)
            delay(500.milliseconds) // let's wait a bit before playing the sound to avoid issues with cutting off audio
        } else if (player.channelId != null && player.channelId != voiceState.channelId) {
            badRequest(call.translate("rest.errors.vc_mismatch"))
        }
        player.playSound(sound, user)
        call.respond(HttpStatusCode.Accepted)
    }
}
