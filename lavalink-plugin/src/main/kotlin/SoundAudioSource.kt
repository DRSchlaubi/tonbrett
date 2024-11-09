package dev.schlaubi.tonbrett.lavalink

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioReference
import kotlin.io.path.absolutePathString
import kotlin.io.path.div
import kotlin.io.path.exists

class SoundAudioSource : LocalAudioSourceManager() {
    override fun loadItem(manager: AudioPlayerManager, reference: AudioReference): AudioItem? {
        val identifier = reference.identifier
        if (!identifier.startsWith("tonbrett:")) return null
        val soundId = identifier.substringAfter(':')

        val soundFile = SoundDownloader.cache / soundId
        if (!soundFile.exists()) return null

        return super.loadItem(manager, AudioReference(soundFile.absolutePathString(), null))
    }

    override fun getSourceName(): String = "tonbrett"
}
