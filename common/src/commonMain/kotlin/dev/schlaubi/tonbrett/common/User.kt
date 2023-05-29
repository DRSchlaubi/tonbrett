package dev.schlaubi.tonbrett.common

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
public data class User(
    val id: SerializableSnowflake,
    val voiceState: VoiceState?
) {
    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    public data class VoiceState(
        // legacy
        @JsonNames("channelMissMatch", "channelMisMatch")
        val channelMismatch: Boolean,
        val channelId: SerializableSnowflake,
        val guildId: SerializableSnowflake,
        val playerAvailable: Boolean,
        val playingSound: Id<Sound>?
    )
}
