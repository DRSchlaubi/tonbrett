package dev.schlaubi.tonbrett.bot.core

import dev.kord.core.behavior.GuildBehavior
import dev.schlaubi.lavakord.UnsafeRestApi
import dev.schlaubi.lavakord.audio.TrackEndEvent
import dev.schlaubi.lavakord.kord.updatePlayer
import dev.schlaubi.lavakord.rest.models.UpdatePlayerRequest
import dev.schlaubi.mikbot.util_plugins.ktor.api.buildBotUrl
import dev.schlaubi.tonbrett.bot.server.sendEvent
import dev.schlaubi.tonbrett.bot.util.player
import dev.schlaubi.tonbrett.common.InterfaceAvailabilityChangeEvent
import dev.schlaubi.tonbrett.common.Snowflake
import dev.schlaubi.tonbrett.common.Sound
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import kotlin.coroutines.CoroutineContext

private val players = mutableMapOf<Snowflake, SoundPlayer>()

val GuildBehavior.soundPlayer: SoundPlayer
    get() = players.getOrPut(id) {
        SoundPlayer(this)
    }

class SoundPlayer(private val guild: GuildBehavior) : CoroutineScope {
    private val player = guild.player
    private var locked: Boolean = false

    @Suppress("INVISIBLE_MEMBER")
    val channelId: Snowflake? get() = player.link.lastChannelId?.let(::Snowflake)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + SupervisorJob()

    private suspend fun updateAvailability(
        available: Boolean, playingSound: Sound? = null,
        channel: Snowflake = channelId ?: error("Cannot use default if not connected")
    ) {
        coroutineScope {
            getUsersInChannel(channel).forEach {
                sendEvent(it, InterfaceAvailabilityChangeEvent(available, playingSound?.id))
            }
        }
    }

    @OptIn(UnsafeRestApi::class)
    @Suppress("INVISIBLE_MEMBER", "SuspendFunctionOnCoroutineScope")
    suspend fun playSound(sound: Sound) {
        require(!locked) { "This player is currently locked" }
        locked = true
        updateAvailability(false, sound)
        val state = player.toState()
        val url = buildBotUrl {
            path("soundboard", "sounds", sound.id.toString(), "audio")
        }.toString()
        player.link.node.updatePlayer(
            guild.id, false,
            UpdatePlayerRequest(identifier = url)
        )
        launch {
            // Wait for track to end
            player.player.events
                .filterIsInstance<TrackEndEvent>()
                .take(1)
                .single()
            state.applyToPlayer(player)
            locked = false
            updateAvailability(true)
        }
    }
}
