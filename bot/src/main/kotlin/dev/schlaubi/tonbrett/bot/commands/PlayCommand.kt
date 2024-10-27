package dev.schlaubi.tonbrett.bot.commands

import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.extensions.unsafeSlashCommand
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule

@OptIn(UnsafeAPI::class)
suspend fun SubCommandModule.playCommand() = unsafeSlashCommand {
    name = "play"
    description = "commands.play.description"

    action {
        event.interaction.openActivity()
    }
}
