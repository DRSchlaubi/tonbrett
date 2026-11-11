package dev.schlaubi.tonbrett.bot

import dev.kord.cache.api.putAll
import dev.kord.gateway.Intent
import dev.kordex.core.builders.ExtensionsBuilder
import dev.kordex.core.extensions.event
import dev.kordex.i18n.Key
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule
import dev.schlaubi.mikbot.plugin.api.util.AllShardsReadyEvent
import dev.schlaubi.mikmusic.core.audio.LavalinkManager
import dev.schlaubi.tonbrett.bot.commands.*
import dev.schlaubi.tonbrett.bot.core.DiscordSoundboardWatcher
import dev.schlaubi.tonbrett.bot.core.VoiceStateWatcher
import dev.schlaubi.tonbrett.bot.core.syncSounds
import dev.schlaubi.tonbrett.bot.server.newServiceKey
import dev.schlaubi.tonbrett.bot.translations.SoundboardTranslations
import dev.schlaubi.tonbrett.common.TonbrettSerializersModule
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.litote.kmongo.serialization.registerModule
import kotlin.time.Duration.Companion.seconds

private val LOG = KotlinLogging.logger { }

@PluginMain
class Plugin(context: PluginContext) : Plugin(context) {
    override fun start() {
        registerModule(TonbrettSerializersModule)

        LOG.info { "Generated new service token: ${newServiceKey()}" }
    }

    override fun ExtensionsBuilder.addExtensions() {
        add(::Module)
        add(::VoiceStateWatcher)
        add(::DiscordSoundboardWatcher)
    }
}

private class Module(context: PluginContext) : SubCommandModule(context) {
    override val name: String = "tonbrett"
    override val commandName: Key = SoundboardTranslations.Commands.Sound.name
    override suspend fun overrideSetup() {
        intents.add(Intent.GuildVoiceStates)
        addCommand()
        removeCommand()
        updateCommand()
        joinCommand()
        playCommand()

        // Request Discord sounds and commit them to the cache
        event<AllShardsReadyEvent> {
            action {
                val defaultSounds = kord.defaultSoundboardSounds
                    .map { it.data }
                kord.cache.putAll(defaultSounds)
                val guilds = kord.guilds.map { it.id }.toList()
                LOG.debug { "Requesting soundboard sounds for ${guilds.size} guilds" }
                val sounds = kord.requestSoundboardSounds(guilds).toList()

                LOG.info { "Successfully fetched ${sounds.size} sounds from Discord" }

                LOG.info { "Waiting 30s for ingresses to ready-up" }
                delay(30.seconds)
                bot.findExtension<LavalinkManager>()!!.lavalink.nodes.forEach {
                    it.syncSounds()
                }
            }
        }
    }
}
