package dev.schlaubi.tonbrett.bot.core

import dev.arbjerg.lavalink.protocol.v4.Message
import dev.kord.core.behavior.GuildBehavior
import dev.schlaubi.lavakord.audio.TrackEndEvent
import dev.schlaubi.mikbot.util_plugins.ktor.api.buildBotUrl
import dev.schlaubi.tonbrett.bot.server.sendEvent
import dev.schlaubi.tonbrett.bot.util.player
import dev.schlaubi.tonbrett.common.InterfaceAvailabilityChangeEvent
import dev.schlaubi.tonbrett.common.Snowflake
import dev.schlaubi.tonbrett.common.Sound
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import kotlin.coroutines.CoroutineContext

private val players = mutableMapOf<Snowflake, SoundPlayer>()

val GuildBehavior.soundPlayer: SoundPlayer
    get() = players.getOrPut(id) {
        SoundPlayer(this)
    }

class SoundPlayer(guild: GuildBehavior) : CoroutineScope {
    val player = guild.player
    var playingSound: Sound? = null
        private set
    var locked: Boolean = false
        private set
    var currentUser: Snowflake? = null
        private set

    val channelId: Snowflake? get() = player.link.lastChannelId?.let(::Snowflake)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + SupervisorJob()

    fun reset() {
        playingSound = null
        locked = false
    }

    private suspend fun updateAvailability(
        available: Boolean, playingSound: Sound? = null, user: Snowflake? = null,
        channel: Snowflake = channelId ?: error("Cannot use default if not connected")
    ) {
        this.playingSound = playingSound
        locked = !available
        coroutineScope {
            getUsersInChannel(channel).forEach {
                sendEvent(it, InterfaceAvailabilityChangeEvent(available || it == user, playingSound?.id))
            }
        }
    }

    suspend fun stop()  {
        updateAvailability(true)
        player.skip()
    }

    @Suppress("INVISIBLE_MEMBER", "SuspendFunctionOnCoroutineScope")
    suspend fun playSound(sound: Sound, user: Snowflake) {
        val alreadyLocked = locked
        locked = true
        updateAvailability(false, sound, user)
        val url = buildBotUrl {
            path("soundboard", "sounds", sound.id.toString(), "audio")
        }.toString()
        currentUser = user
        player.injectTrack(url, noReplace = alreadyLocked)
        launch {
            // Wait for track to end
            player.player.events
                .filterIsInstance<TrackEndEvent>()
                .filter { it.reason != Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.REPLACED }
                .take(1)
                .single()
            locked = false
            currentUser = null
            if (channelId != null) {
                updateAvailability(true)
            }
        }
    }
}
