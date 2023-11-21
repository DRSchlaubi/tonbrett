package dev.schlaubi.tonbrett.common

import dev.schlaubi.tonbrett.common.util.formatEmojiUnicode
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonClassDiscriminator

/**
 * Representation of a sound.
 *
 * @property id the id of this sound
 * @property name the name of this sound
 * @property owner the id of the user who owns this sound
 * @property description an optional description of this sound
 * @property emoji an optional [Emoji] representing this sound
 * @property tag an optional tag for this sound
 * @property volume the volume used to play the sound
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
    val tag: String? = null,
    val volume: Int = 100
) {
    /**
     * The file name of this sounds audio file.
     */
    val fileName: String get() = "$id.audio"

    /**
     * Representation of either a [GuildEmoji] or a [DiscordEmoji].
     */
    @Serializable
    @JsonClassDiscriminator("type")
    public sealed interface Emoji {
        /**
         * Subinterface of [Emoji] specifying this emoji has an image url.
         *
         * @property url a CDN url to this emojis image file
         */
        public sealed interface HasUrl {
            public val url: String
        }
    }

    /**
     * Representation of a Discord standard emoji.
     *
     * @property value the unicode character of the emoji
     */
    @Serializable
    @SerialName("discord")
    public data class DiscordEmoji(val value: String) : Emoji

    /**
     * Representation of a Twemoji
     *
     * @property value the raw value of the emoji (unicode)
     * @property codePoints a [Sequence] of all code points
     * @property length the length of the string
     */
    @Serializable
    @SerialName("twemoji")
    public data class Twemoji(
        val value: String,
        val codePoints: @Serializable(with = SequenceSerializer::class) Sequence<Int>,
        val length: Int
    ) : Emoji, Emoji.HasUrl {
        override val url: String
            get() {
                val name = formatEmojiUnicode(codePoints, length)
                return "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/$name.png"
            }
    }

    /**
     * Representation of a Guild custom emoji.
     *
     * @property id the id of the emoji
     * @property isAnimated whether the emoji is animated or not
     */
    @Serializable
    @SerialName("guild")
    public data class GuildEmoji(val id: SerializableSnowflake, val isAnimated: Boolean = false) : Emoji, Emoji.HasUrl {
        override val url: String
            get() = buildString {
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

/**
 * Representation of a grouping of sounds.
 *
 * @property tag the tag of all sounds in the group
 * @property sounds a list of [Sounds][Sound] in this group@
 */
@Serializable
public data class SoundGroup(
    @SerialName("_id")
    val tag: String?,
    val sounds: List<Sound>
)

internal class SequenceSerializer<T>(childSerializer: KSerializer<T>) : KSerializer<Sequence<T>> {
    private val delegate = ListSerializer(childSerializer)
    override val descriptor: SerialDescriptor
        get() = delegate.descriptor

    override fun serialize(encoder: Encoder, value: Sequence<T>): Unit = delegate.serialize(encoder, value.toList())

    override fun deserialize(decoder: Decoder): Sequence<T> =
        delegate.deserialize(decoder).asSequence()
}
