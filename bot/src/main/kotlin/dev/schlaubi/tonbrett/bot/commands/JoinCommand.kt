package dev.schlaubi.tonbrett.bot.commands

import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule
import dev.schlaubi.mikmusic.checks.joinSameChannelCheck

suspend fun SubCommandModule.joinCommand() = ephemeralSlashCommand {
    name = "join"
    description = "commands.join.description"

    check {
        joinSameChannelCheck(bot)
    }

    action {
        respond {
            content = translate("commands.join.success")
        }
    }
}
