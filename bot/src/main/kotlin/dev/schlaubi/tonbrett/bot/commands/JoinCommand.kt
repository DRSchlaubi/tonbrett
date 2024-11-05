package dev.schlaubi.tonbrett.bot.commands

import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikmusic.checks.joinSameChannelCheck
import dev.schlaubi.tonbrett.bot.translations.SoundboardTranslations

suspend fun SubCommandModule.joinCommand() = ephemeralSlashCommand {
    name = SoundboardTranslations.Commands.Join.name
    description = SoundboardTranslations.Commands.Join.description

    check {
        joinSameChannelCheck(bot)
    }

    action {
        respond {
            content = translate(SoundboardTranslations.Commands.Join.success)
        }
    }
}
