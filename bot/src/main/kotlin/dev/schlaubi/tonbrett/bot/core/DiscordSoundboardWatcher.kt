package dev.schlaubi.tonbrett.bot.core

import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.event.guild.GuildSoundboardSoundCreateEvent
import dev.kord.core.event.guild.GuildSoundboardSoundDeletEvent
import dev.kord.core.event.guild.GuildSoundboardSoundUpdateEvent
import dev.kord.core.event.guild.VoiceChannelEffectSentEvent
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.event
import dev.schlaubi.tonbrett.bot.server.broadcastEvent
import dev.schlaubi.tonbrett.bot.util.toSound
import dev.schlaubi.tonbrett.common.SoundCreatedEvent
import dev.schlaubi.tonbrett.common.SoundDeletedEvent
import dev.schlaubi.tonbrett.common.SoundUpdatedEvent
import dev.schlaubi.tonbrett.common.toId

class DiscordSoundboardWatcher : Extension() {
    override val name: String = "soundboard-watcher"

    @OptIn(KordExperimental::class, KordUnsafe::class)
    override suspend fun setup() {
        event<GuildSoundboardSoundCreateEvent> {
            action {
                broadcastEvent(SoundCreatedEvent(event.sound.data.toSound()))
            }
        }

        event<GuildSoundboardSoundUpdateEvent> {
            action {
                broadcastEvent(SoundUpdatedEvent(event.sound.data.toSound()))
            }
        }

        event<GuildSoundboardSoundDeletEvent> {
            action {
                broadcastEvent(SoundDeletedEvent(event.soundId.toId()))
            }
        }
    }
}