@file:OptIn(UnsafeAPI::class)

package dev.schlaubi.tonbrett.bot

import dev.kordex.core.extensions.primaryEntryPointCommand
import dev.kord.common.entity.PrimaryEntryPointCommandHandlerType
import dev.kord.gateway.Intent
import dev.kordex.core.builders.ExtensionsBuilder
import dev.kordex.core.i18n.toKey
import dev.kordex.core.i18n.types.Key
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule
import dev.schlaubi.tonbrett.bot.commands.*
import dev.schlaubi.tonbrett.bot.core.VoiceStateWatcher
import dev.schlaubi.tonbrett.bot.translations.SoundboardTranslations
import dev.schlaubi.tonbrett.common.TonbrettSerializersModule
import org.litote.kmongo.serialization.registerModule

@PluginMain
class Plugin(context: PluginContext) : Plugin(context) {
    override fun start() {
        registerModule(TonbrettSerializersModule)
    }

    override fun ExtensionsBuilder.addExtensions() {
        add(::Module)
        add(::VoiceStateWatcher)
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

        primaryEntryPointCommand {
            name = "Tonbrett".toKey()
            description = SoundboardTranslations.Commands.EntryPoint.description

            handler = PrimaryEntryPointCommandHandlerType.DiscordLaunchActivity
        }
    }
}
