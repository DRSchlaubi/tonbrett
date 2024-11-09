package dev.schlaubi.tonbrett.bot.core

import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.event.guild.GuildSoundboardSoundCreateEvent
import dev.kord.core.event.guild.GuildSoundboardSoundDeletEvent
import dev.kord.core.event.guild.GuildSoundboardSoundUpdateEvent
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.event
import dev.schlaubi.tonbrett.bot.server.broadcastEvent
import dev.schlaubi.tonbrett.bot.util.toSound
import dev.schlaubi.tonbrett.common.SoundCreatedEvent
import dev.schlaubi.tonbrett.common.SoundDeletedEvent
import dev.schlaubi.tonbrett.common.SoundUpdatedEvent
import dev.schlaubi.tonbrett.common.toId
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope

class DiscordSoundboardWatcher : Extension() {
    override val name: String = "soundboard-watcher"

    @OptIn(KordExperimental::class, KordUnsafe::class)
    override suspend fun setup() {
        event<GuildSoundboardSoundCreateEvent> {
            action {
                coroutineScope {
                    val sound = event.sound.data.toSound()
                    launch {
                        bot.syncSound(sound)
                    }
                    launch {
                        broadcastEvent(SoundCreatedEvent(sound))
                    }
                }
            }
        }

        event<GuildSoundboardSoundUpdateEvent> {
            action {
                broadcastEvent(SoundUpdatedEvent(event.sound.data.toSound()))
            }
        }

        event<GuildSoundboardSoundDeletEvent> {
            action {
                val id = event.soundId
                coroutineScope {
                    launch {
                        bot.deleteSound(id.toString())
                    }
                    launch {
                        broadcastEvent(SoundDeletedEvent(id.toId()))
                    }
                }
            }
        }
    }
}
