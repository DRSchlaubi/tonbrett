package dev.schlaubi.tonbrett.common

import kotlinx.serialization.*
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
public data class Sound(
    @SerialName("_id")
    val id: Id<Sound>,
    val name: String,
    val owner: @Contextual Snowflake,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val description: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val emoji: Emoji? = null
) {
    val fileName: String get() = "$id.audio"

    @Serializable
    @JsonClassDiscriminator("type")
    public sealed interface Emoji

    @Serializable
    @SerialName("guild")
    public data class GuildEmoji(val value: String) : Emoji

    @Serializable
    @SerialName("discord")
    public data class DiscordEmoji(val value: String) : Emoji
}
