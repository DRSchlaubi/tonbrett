package dev.schlaubi.tonbrett.common.util

import dev.schlaubi.tonbrett.common.Sound
import kotlin.streams.asSequence

@Suppress("USELESS_CAST") // IntelliJ is weird here
private fun Sound.DiscordEmoji.toTwemoji(): Sound.Twemoji =
    Sound.Twemoji(value, (value as String).codePoints().asSequence(), value.length)

/**
 * Converts this emoji to contain information needed on non JVM Platforms.
 */
public fun Sound.convertForNonJvmPlatforms(): Sound = if (emoji is Sound.DiscordEmoji) {
    copy(emoji = emoji.toTwemoji())
} else {
    this
}

/**
 * Converts all emojis in this [List] to contain information needed on non JVM Platforms.
 */
public fun List<Sound>.convertForNonJvmPlatforms(condition: Boolean = true): List<Sound> =
    if (condition) map(Sound::convertForNonJvmPlatforms) else this
