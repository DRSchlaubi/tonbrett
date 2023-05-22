package dev.schlaubi.tonbrett.bot

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import dev.kord.gateway.Intent
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule
import dev.schlaubi.tonbrett.bot.commands.*
import dev.schlaubi.tonbrett.bot.core.VoiceStateWatcher

@PluginMain
class Plugin(context: PluginContext) : Plugin(context) {
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
    }
}
