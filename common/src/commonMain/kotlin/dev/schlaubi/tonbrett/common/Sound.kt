package dev.schlaubi.tonbrett.common

import dev.schlaubi.tonbrett.common.util.formatEmojiUnicode
import kotlinx.serialization.*
import kotlinx.serialization.json.JsonClassDiscriminator

/**
 * Representation of a sound.
 *
 * @property id the id of this sound
 * @property name the name of this sound
 * @property owner the id of the user who owns this sound
 * @property description an optional description of this sound
 * @property emoji an optional [Emoji] representing this sound
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
public data class Sound(
    @SerialName("_id")
    val id: Id<Sound>,
    val name: String,
    val owner: SerializableSnowflake,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val description: String? = null,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val emoji: Emoji? = null,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val public: Boolean = true,
    val tag: String? = null
) {
    /**
     * The file name of this sounds audio file.
     */
    val fileName: String get() = "$id.audio"

    /**
     * Representation of either a [GuildEmoji] or a [DiscordEmoji].
     *
     * @property url the URL to the image file of this emoji
     */
    @Serializable
    @JsonClassDiscriminator("type")
    public sealed interface Emoji {
        public val url: String
    }

    /**
     * Representation of a Discord standard emoji.
     *
     * @property value the unicode character of the emoji
     */
    @Serializable
    @SerialName("discord")
    public data class DiscordEmoji(val value: String) : Emoji {
        override val url: String get() =
            "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/${formatEmojiUnicode(value)}.png"
    }

    /**
     * Representation of a Guild custom emoji.
     *
     * @property id the id of the emoji
     */
    @Serializable
    @SerialName("guild")
    public data class GuildEmoji(val id: SerializableSnowflake, val isAnimated: Boolean = false) : Emoji {
        override val url: String get() = buildString {
            append("https://cdn.discordapp.com/emojis/")
            if (isAnimated) {
                append("a_")
            }
            append(id)
            append('.')
            if (isAnimated) {
                append("gif")
            } else {
                append("png")
            }
        }
    }
}
