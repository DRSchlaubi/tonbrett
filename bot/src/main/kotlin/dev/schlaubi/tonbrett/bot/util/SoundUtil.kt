package dev.schlaubi.tonbrett.bot.util

import dev.kord.core.cache.data.SoundboardSoundData
import dev.schlaubi.tonbrett.common.Sound
import dev.schlaubi.tonbrett.common.toId

fun SoundboardSoundData.toSound() = Sound(
    id.toId<Sound>(),
    name,
    user.value?.id,
    null,
    toEmoji(),
    public = true,
    isDiscord = true,
    tag = null,
    volume = (volume * 1000).toInt() // discord uses a number between 0 and 1
)

private fun SoundboardSoundData.toEmoji() = when {
    emojiId != null -> Sound.GuildEmoji(emojiId!!)
    emojiName != null -> Sound.DiscordEmoji(emojiName!!)
    else -> null
}
