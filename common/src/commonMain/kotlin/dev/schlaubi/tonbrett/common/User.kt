package dev.schlaubi.tonbrett.common

import kotlinx.serialization.Serializable

@Serializable
public data class User(
    val id: SerializableSnowflake,
    val language: String?,
    val voiceState: VoiceState?
) {
    @Serializable
    public data class VoiceState(
        val guildId: SerializableSnowflake,
        val channelId: SerializableSnowflake
    )
}
