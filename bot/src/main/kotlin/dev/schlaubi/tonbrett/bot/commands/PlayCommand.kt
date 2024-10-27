package dev.schlaubi.tonbrett.bot.commands

import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule

@OptIn(UnsafeAPI::class)
fun SubCommandModule.playCommand() = unsafeSubCommand("play") {
    name = "play"
    description = "commands.play.description"

    action {
        event.interaction.openActivity()
    }
}
