package dev.schlaubi.tonbrett.bot.commands

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.optionalBoolean
import dev.kordex.core.commands.converters.impl.optionalInt
import dev.kordex.core.commands.converters.impl.optionalString
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.tonbrett.bot.command.emoji
import dev.schlaubi.tonbrett.bot.command.sound
import dev.schlaubi.tonbrett.bot.command.tagArgument
import dev.schlaubi.tonbrett.bot.command.toEmoji
import dev.schlaubi.tonbrett.bot.io.SoundBoardDatabase
import dev.schlaubi.tonbrett.bot.server.broadcastEvent
import dev.schlaubi.tonbrett.bot.translations.SoundboardTranslations
import dev.schlaubi.tonbrett.common.Sound
import dev.schlaubi.tonbrett.common.SoundUpdatedEvent
import dev.schlaubi.tonbrett.common.util.convertForNonJvmPlatforms
import org.litote.kmongo.and
import org.litote.kmongo.eq

class UpdateSoundArguments : Arguments() {
    val sound by sound {
        name = SoundboardTranslations.Commands.UpdateSound.Arguments.Sound.name
        description = SoundboardTranslations.Commands.UpdateSound.Arguments.Sound.description
    }

    val name by optionalString {
        name = SoundboardTranslations.Commands.UpdateSound.Arguments.Name.name
        description = SoundboardTranslations.Commands.UpdateSound.Arguments.Name.description
        maxLength = NAME_MAX_LENGTH
    }

    val description by optionalString {
        name = SoundboardTranslations.Commands.UpdateSound.Arguments.Description.name
        description = SoundboardTranslations.Commands.UpdateSound.Arguments.Description.description
    }

    val emoji by emoji(SoundboardTranslations.Commands.UpdateSound.Arguments.Emoji.name, SoundboardTranslations.Commands.UpdateSound.Arguments.Emoji.description)

    val tag by tagArgument(SoundboardTranslations.Commands.UpdateSound.Arguments.Tag.name, SoundboardTranslations.Commands.UpdateSound.Arguments.Tag.description)

    val public by optionalBoolean {
        name = SoundboardTranslations.Commands.UpdateSound.Arguments.Public.name
        description = SoundboardTranslations.Commands.UpdateSound.Arguments.Public.description
    }

    val volume by optionalInt {
        name = SoundboardTranslations.Commands.UpdateSound.Arguments.Volume.name
        description = SoundboardTranslations.Commands.UpdateSound.Arguments.Volume.description
        minValue = 0
        maxValue = 1000
    }
}

fun SubCommandModule.updateCommand() = ephemeralSubCommand(::UpdateSoundArguments) {
    name = SoundboardTranslations.Commands.UpdateSound.name
    description = SoundboardTranslations.Commands.UpdateSound.description

    action {
        if (arguments.name != null) {
            val foundByName = SoundBoardDatabase.sounds.findOne(
                and(
                    Sound::owner eq user.id,
                    Sound::name eq arguments.name
                )
            )

            if (foundByName != null) {
                discordError(SoundboardTranslations.Commands.UpdateSound.nameInUse.withOrdinalPlaceholders(arguments.name))
            }
        }

        val sound = arguments.sound
        val newSound = sound.copy(
            name = arguments.name ?: sound.name,
            description = arguments.description ?: sound.description,
            emoji = arguments.emoji?.toEmoji() ?: sound.emoji,
            public = arguments.public ?: sound.public,
            tag = arguments.tag ?: sound.tag,
            volume = arguments.volume ?: sound.volume
        )

        SoundBoardDatabase.sounds.save(newSound)
        respond {
            content = translate(SoundboardTranslations.Commands.UpdateSound.success)
        }

        broadcastEvent(SoundUpdatedEvent(newSound.convertForNonJvmPlatforms()))
    }
}
