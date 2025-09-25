package dev.schlaubi.tonbrett.common.util

import dev.schlaubi.tonbrett.common.Sound
import dev.schlaubi.tonbrett.common.SoundGroup
import java.util.stream.Collectors

@Suppress("USELESS_CAST") // IntelliJ is weird here
private fun Sound.DiscordEmoji.toTwemoji(): Sound.Twemoji {
    val codePoints = (value as java.lang.String).codePoints().boxed().collect(Collectors.toList())

    return Sound.Twemoji(value, codePoints, value.length)
}

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

/**
 * Converts all emojis in this [List] to contain information needed on non JVM Platforms.
 */
@JvmName("convertSoundGroupForNonJvmPlatforms")
public fun List<SoundGroup>.convertForNonJvmPlatforms(condition: Boolean = true): List<SoundGroup> =
    if (condition) map { it.copy(sounds = it.sounds.convertForNonJvmPlatforms()) } else this
