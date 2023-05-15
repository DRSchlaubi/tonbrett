package dev.schlaubi.tonbrett.bot

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.module.MikBotModule
import dev.schlaubi.tonbrett.bot.commands.addSoundCommand

@PluginMain
class Plugin(context: PluginContext) : Plugin(context) {
    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::Module)
    }
}

private class Module(context: PluginContext) : MikBotModule(context) {
    override val name: String = "tonbrett"
    override val bundle: String = "soundboard"

    override suspend fun setup() {
        addSoundCommand()
    }
}
