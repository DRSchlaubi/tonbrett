package dev.schlaubi.tonbrett.lavalink

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import dev.arbjerg.lavalink.api.AudioPlayerManagerConfiguration
import org.springframework.stereotype.Component

@Component
class Plugin : AudioPlayerManagerConfiguration {
    override fun configure(manager: AudioPlayerManager): AudioPlayerManager = manager.apply {
        registerSourceManager(SoundAudioSource())
    }
}
