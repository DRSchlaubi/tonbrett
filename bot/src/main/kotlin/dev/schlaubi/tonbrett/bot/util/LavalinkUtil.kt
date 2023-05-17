package dev.schlaubi.tonbrett.bot.util

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.koin.KordExContext
import dev.kord.core.behavior.GuildBehavior
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.player.MusicPlayer

val GuildBehavior.player: MusicPlayer
    get() = KordExContext.get().get<ExtensibleBot>()
        .findExtension<MusicModule>()!!.getMusicPlayer(this)
