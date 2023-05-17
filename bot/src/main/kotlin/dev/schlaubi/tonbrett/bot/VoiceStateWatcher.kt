package dev.schlaubi.tonbrett.bot

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.entity.DiscordUser
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.schlaubi.tonbrett.common.Snowflake
import dev.schlaubi.tonbrett.common.User

private val voiceStateCache = mutableMapOf<Snowflake, User.VoiceState>()

val DiscordUser.voiceState: User.VoiceState?
    get() = voiceStateCache[id]

class VoiceStateWatcher : Extension() {
    override val name: String = "voice_state_watcher"

    override suspend fun setup() {
        event<VoiceStateUpdateEvent> {
            action {
                val voiceState = event.state
                if (voiceState.channelId == null) {
                    voiceStateCache.remove(voiceState.userId)
                } else {
                    voiceStateCache[voiceState.userId] =
                        User.VoiceState(voiceState.guildId, voiceState.channelId!!)
                }
            }
        }
    }
}
