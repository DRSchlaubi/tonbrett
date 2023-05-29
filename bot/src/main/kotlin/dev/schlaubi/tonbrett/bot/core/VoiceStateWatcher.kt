package dev.schlaubi.tonbrett.bot.core

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.koin.KordExContext
import dev.kord.cache.api.query
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.DiscordUser
import dev.kord.core.Kord
import dev.kord.core.cache.data.VoiceStateData
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.schlaubi.tonbrett.bot.server.sendEvent
import dev.schlaubi.tonbrett.common.Snowflake
import dev.schlaubi.tonbrett.common.User
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import dev.schlaubi.tonbrett.common.VoiceStateUpdateEvent as APIVoiceStateUpdateEvent

private val kord: Kord by KordExContext.get().inject<Kord>()

val Snowflake.voiceState: User.VoiceState?
    get() = runBlocking { findVoiceState(this@voiceState) }

@OptIn(KordUnsafe::class, KordExperimental::class)
suspend fun findVoiceState(userId: Snowflake): User.VoiceState? {
    val result = kord.cache.query<VoiceStateData> {
        VoiceStateData::channelId ne null
        VoiceStateData::userId eq userId
    }.singleOrNull() ?: return null

    val botState = kord.unsafe.guild(result.guildId).soundPlayer
    val botChannel = botState.channelId
    val playerAvailable = !botState.locked || botState.currentUser == userId
    val botOffline = botChannel == null
    val channelMismatch = botChannel != result.channelId

    return User.VoiceState(
        !botOffline && channelMismatch,
        result.channelId!!, result.guildId, playerAvailable, botState.playingSound?.id
    )
}

suspend fun getUsersInChannel(channelId: Snowflake): List<Snowflake> = kord.cache.query<VoiceStateData> {
    VoiceStateData::channelId eq channelId
}.toCollection().map { it.userId }

@JvmName("getUsersInChannelNullable")
suspend fun getUsersInChannel(channelId: Snowflake?): List<Snowflake> =
    channelId?.let { getUsersInChannel(it) } ?: emptyList()

class VoiceStateWatcher : Extension() {
    override val name: String = "voice_state_watcher"

    @OptIn(KordUnsafe::class, KordExperimental::class)
    override suspend fun setup() {
        event<VoiceStateUpdateEvent> {
            check {
                // Ignore changes without channel change
                failIf { event.state.channelId == event.old?.channelId }
            }
            action {
                if (event.state.userId == kord.selfId) {
                    val oldChannel = event.old?.channelId
                    val player = event.kord.unsafe.guild(event.state.guildId).soundPlayer
                    if (event.state.channelId == null) {
                        player.reset()
                    }
                    coroutineScope {
                        (getUsersInChannel(oldChannel) + getUsersInChannel(event.state.channelId)).forEach {
                            launch {
                                val voiceState = findVoiceState(it)
                                sendEvent(it, APIVoiceStateUpdateEvent(voiceState))
                            }
                        }
                    }
                } else {
                    val voiceState = event.state
                    val botState = event.state.getGuild().soundPlayer
                    val botChannelId = botState.channelId
                    val botOffline = botChannelId == null
                    val channelMismatch = botChannelId != voiceState.channelId
                    if (voiceState.channelId == null) {
                        sendEvent(voiceState.userId, APIVoiceStateUpdateEvent(null))
                    } else {
                        val userState = User.VoiceState(
                            !botOffline && channelMismatch,
                            voiceState.channelId!!,
                            voiceState.guildId,
                            !botState.locked,
                            botState.playingSound?.id
                        )

                        sendEvent(
                            voiceState.userId,
                            APIVoiceStateUpdateEvent(userState)
                        )
                    }
                }
            }
        }
    }
}
