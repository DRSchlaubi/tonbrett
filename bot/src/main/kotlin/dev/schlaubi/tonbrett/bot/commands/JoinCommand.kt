package dev.schlaubi.tonbrett.bot.commands

import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule
import dev.schlaubi.mikmusic.checks.joinSameChannelCheck

fun SubCommandModule.joinCommand() = ephemeralSubCommand {
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
