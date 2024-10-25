package dev.schlaubi.tonbrett.bot

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.extensions.primaryEntryPointCommand
import dev.kord.common.entity.EntryPointCommandHandlerType
import dev.kord.gateway.Intent
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule
import dev.schlaubi.tonbrett.bot.commands.*
import dev.schlaubi.tonbrett.bot.core.VoiceStateWatcher
import dev.schlaubi.tonbrett.common.TonbrettSerializersModule
import org.litote.kmongo.serialization.registerModule

@PluginMain
class Plugin(context: PluginContext) : Plugin(context) {
    override fun start() {
        registerModule(TonbrettSerializersModule)
    }

    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::Module)
        add(::VoiceStateWatcher)
    }
}

private class Module(context: PluginContext) : SubCommandModule(context) {
    override val name: String = "tonbrett"
    override val bundle: String = "soundboard"
    override val commandName: String = "sound"
    override suspend fun overrideSetup() {
        intents.add(Intent.GuildVoiceStates)
        addCommand()
        removeCommand()
        updateCommand()
        joinCommand()
        playCommand()

        primaryEntryPointCommand {
            name = "Tonbrett"
            description = "commands.entry_point.description"

            handler = EntryPointCommandHandlerType.DiscordLaunchActivity
        }
    }
}
