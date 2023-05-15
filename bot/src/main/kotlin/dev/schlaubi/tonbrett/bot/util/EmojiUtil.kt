package dev.schlaubi.tonbrett.bot.util

import dev.kord.core.entity.GuildEmoji
import dev.schlaubi.tonbrett.common.Sound

fun GuildEmoji.toEmoji(): Sound.Emoji = Sound.GuildEmoji(mention)
