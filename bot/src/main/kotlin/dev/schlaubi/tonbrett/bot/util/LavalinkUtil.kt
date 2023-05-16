package dev.schlaubi.tonbrett.bot.util

import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.behavior.GuildBehavior
import dev.schlaubi.mikmusic.player.MusicPlayer
import dev.schlaubi.mikmusic.util.musicModule

context(Extension)
val GuildBehavior.player: MusicPlayer
    get() = musicModule.getMusicPlayer(this)
