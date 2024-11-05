package dev.schlaubi.tonbrett.bot.util

import dev.kordex.core.ExtensibleBot
import dev.kordex.core.koin.KordExContext
import dev.kord.core.behavior.GuildBehavior
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.player.MusicPlayer

val GuildBehavior.player: MusicPlayer
    get() = KordExContext.get().get<ExtensibleBot>()
        .findExtension<MusicModule>()!!.getMusicPlayer(this)
