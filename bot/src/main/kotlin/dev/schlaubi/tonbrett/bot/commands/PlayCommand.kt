package dev.schlaubi.tonbrett.bot.commands

import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule
import dev.schlaubi.tonbrett.bot.translations.SoundboardTranslations

@OptIn(UnsafeAPI::class)
fun SubCommandModule.playCommand() = unsafeSubCommand {
    name = SoundboardTranslations.Commands.Play.name
    description = SoundboardTranslations.Commands.Play.description

    action {
        event.interaction.openActivity()
    }
}
