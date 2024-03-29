package dev.schlaubi.tonbrett.bot.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.tonbrett.bot.command.emoji
import dev.schlaubi.tonbrett.bot.command.sound
import dev.schlaubi.tonbrett.bot.command.tagArgument
import dev.schlaubi.tonbrett.bot.command.toEmoji
import dev.schlaubi.tonbrett.bot.io.SoundBoardDatabase
import dev.schlaubi.tonbrett.bot.server.broadcastEvent
import dev.schlaubi.tonbrett.common.Sound
import dev.schlaubi.tonbrett.common.SoundUpdatedEvent
import dev.schlaubi.tonbrett.common.util.convertForNonJvmPlatforms
import org.litote.kmongo.and
import org.litote.kmongo.eq

class UpdateSoundArguments : Arguments() {
    val sound by sound {
        name = "sound"
        description = "commands.update_sound.arguments.sound.description"
    }

    val name by optionalString {
        name = "name"
        description = "commands.update_sound.arguments.name.description"
        maxLength = NAME_MAX_LENGTH
    }

    val description by optionalString {
        name = "description"
        description = "commands.update_sound.arguments.description.description"
    }

    val emoji by emoji("emoji", "commands.update_sound.arguments.emoji.description")

    val tag by tagArgument("tag", "commands.update_sound.arguments.tag.description")

    val public by optionalBoolean {
        name = "public"
        description = "commands.update_sound.arguments.public.description"
    }

    val volume by optionalInt {
        name = "volume"
        description = "commands.update_sound.arguments.volume.description"
        minValue = 0
        maxValue = 1000
    }
}

fun SubCommandModule.updateCommand() = ephemeralSubCommand(::UpdateSoundArguments) {
    name = "update"
    description = "commands.update_sound.description"

    action {
        if (arguments.name != null) {
            val foundByName = SoundBoardDatabase.sounds.findOne(
                and(
                    Sound::owner eq user.id,
                    Sound::name eq arguments.name
                )
            )

            if (foundByName != null) {
                discordError(translate("commands.update_sound.name_in_use", arrayOf(arguments.name)))
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
            content = translate("commands.update_sound.success")
        }

        broadcastEvent(SoundUpdatedEvent(newSound.convertForNonJvmPlatforms()))
    }
}
