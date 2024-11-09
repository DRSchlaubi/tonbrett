 package dev.schlaubi.tonbrett.bot.commands

import dev.kordex.core.commands.Arguments
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.tonbrett.bot.command.sound
import dev.schlaubi.tonbrett.bot.core.deleteSound
import dev.schlaubi.tonbrett.bot.core.syncSound
import dev.schlaubi.tonbrett.bot.io.SoundBoardDatabase
import dev.schlaubi.tonbrett.bot.server.broadcastEvent
import dev.schlaubi.tonbrett.bot.translations.SoundboardTranslations
import dev.schlaubi.tonbrett.common.SoundCreatedEvent
import dev.schlaubi.tonbrett.common.SoundDeletedEvent
import dev.schlaubi.tonbrett.common.util.convertForNonJvmPlatforms
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

 class RemoveSoundArguments : Arguments() {
    val sound by sound {
        name = SoundboardTranslations.Commands.RemoveSound.Arguments.Sound.name
        description = SoundboardTranslations.Commands.RemoveSound.Arguments.Sound.description
    }
}

fun SubCommandModule.removeCommand() = ephemeralSubCommand(::RemoveSoundArguments) {
    name = SoundboardTranslations.Commands.Remove.name
    description = SoundboardTranslations.Commands.Remove.description

    action {
        SoundBoardDatabase.sounds.deleteOneById(arguments.sound.id)

        respond {
            content = translate(SoundboardTranslations.Commands.RemoveSound.success)
        }

        coroutineScope {
            launch {
                bot.deleteSound(arguments.sound.id.toString())
            }
            launch {
                broadcastEvent(SoundDeletedEvent(arguments.sound.id))
            }
        }

    }
}
