package dev.schlaubi.tonbrett.bot.core

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.entity.DiscordUser
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.schlaubi.tonbrett.bot.server.sendEvent
import dev.schlaubi.tonbrett.common.InterfaceAvailabilityChangeEvent
import dev.schlaubi.tonbrett.common.Snowflake
import dev.schlaubi.tonbrett.common.User

private val voiceStateCache = mutableMapOf<Snowflake, User.VoiceState>()

val DiscordUser.voiceState: User.VoiceState?
    get() = voiceStateCache[id]

fun getUsersInChannel(channelId: Snowflake) = voiceStateCache
    .toList()
    .filter { (_, state) -> state.channelId == channelId }
    .map { (id) -> id }

class VoiceStateWatcher : Extension() {
    override val name: String = "voice_state_watcher"

    override suspend fun setup() {
        event<VoiceStateUpdateEvent> {
            action {
                val voiceState = event.state
                val botChannelId = event.state.getGuild().soundPlayer.channelId
                val botOnline = botChannelId == null
                if (voiceState.channelId == null) {
                    voiceStateCache.remove(voiceState.userId)
                    sendEvent(voiceState.userId, InterfaceAvailabilityChangeEvent(false, !botOnline))
                } else {
                    voiceStateCache[voiceState.userId] =
                        User.VoiceState(!botOnline, botChannelId != voiceState.channelId, voiceState.channelId!!)
                    sendEvent(
                        voiceState.userId,
                        InterfaceAvailabilityChangeEvent(botChannelId == voiceState.channelId, !botOnline)
                    )
                }
            }
        }
    }
}
