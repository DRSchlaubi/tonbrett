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
        val botOffline: Boolean,
        val channelMissMatch: Boolean,
        val channelId: SerializableSnowflake,
        val guildId: SerializableSnowflake,
        val playerAvailable: Boolean,
        val playingSound: Id<Sound>?
    )
}
