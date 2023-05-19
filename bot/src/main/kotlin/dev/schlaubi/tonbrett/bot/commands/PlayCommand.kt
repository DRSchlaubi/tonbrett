package dev.schlaubi.tonbrett.bot.commands

import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.actionRow
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule
import dev.schlaubi.mikbot.util_plugins.ktor.api.buildBotUrl
import io.ktor.http.*

fun SubCommandModule.playCommand() = ephemeralSubCommand {
    name = "play"
    description = "commands.play.description"

    action {
        respond {
            content = translate("commands.play")

            actionRow {
                linkButton(buildBotUrl {
                    path("soundboard", "ui")
                }.toString()) {
                    label = translate("commands.play.button")
                }
            }
        }
    }
}
