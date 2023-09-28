package dev.schlaubi.tonbrett.common

import dev.schlaubi.tonbrett.common.User.VoiceState
import kotlinx.serialization.Serializable

/**
 * Representation of a user.
 *
 * @property id the id of the user
 * @property VoiceState the current [VoiceState] of the user or `null` if they aren't connected
 */
@Serializable
public data class User(
    val id: SerializableSnowflake,
    val voiceState: VoiceState?
) {

    /**
     * Representation of a voice state.
     *
     * @property channelMismatch whether the bot and the users channel mismatch
     * @property channelId the users channel id
     * @property guildId the Guild id of this voice state
     * @property playerAvailable whether the player is available
     * @property playingSound the id of the currently playing sound or `null`
     */
    @Serializable
    public data class VoiceState(
        val channelMismatch: Boolean,
        val channelId: SerializableSnowflake,
        val guildId: SerializableSnowflake,
        val playerAvailable: Boolean,
        val playingSound: Id<Sound>?
    )
}
