package dev.schlaubi.tonbrett.bot.commands

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.*
import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule
import dev.schlaubi.mikbot.plugin.api.util.kord
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.tonbrett.bot.command.emoji
import dev.schlaubi.tonbrett.bot.command.tagArgument
import dev.schlaubi.tonbrett.bot.command.toEmoji
import dev.schlaubi.tonbrett.bot.config.Config
import dev.schlaubi.tonbrett.bot.io.SoundBoardDatabase
import dev.schlaubi.tonbrett.bot.server.broadcastEvent
import dev.schlaubi.tonbrett.bot.translations.SoundboardTranslations
import dev.schlaubi.tonbrett.bot.util.player
import dev.schlaubi.tonbrett.common.Sound
import dev.schlaubi.tonbrett.common.SoundCreatedEvent
import dev.schlaubi.tonbrett.common.newId
import dev.schlaubi.tonbrett.common.util.convertForNonJvmPlatforms
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.litote.kmongo.and
import org.litote.kmongo.eq
import java.nio.file.StandardOpenOption
import kotlin.io.path.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class AddSoundCommandArguments : Arguments() {
    val sound by attachment {
        name = SoundboardTranslations.Commands.AddSound.Arguments.Attachment.name
        description = SoundboardTranslations.Commands.AddSound.Arguments.Attachment.description
    }

    val name by string {
        name = SoundboardTranslations.Commands.AddSound.Arguments.Name.name
        description = SoundboardTranslations.Commands.AddSound.Arguments.Name.description
        maxLength = NAME_MAX_LENGTH
    }

    val description by optionalString {
        name = SoundboardTranslations.Commands.AddSound.Arguments.Description.name
        description = SoundboardTranslations.Commands.AddSound.Arguments.Description.description
    }

    val emoji by emoji(SoundboardTranslations.Commands.AddSound.Arguments.Emoji.name, SoundboardTranslations.Commands.AddSound.Arguments.Emoji.description)

    val tag by tagArgument(SoundboardTranslations.Commands.AddSound.Arguments.Tag.name, SoundboardTranslations.Commands.AddSound.Arguments.Tag.description)

    val public by defaultingBoolean {
        name = SoundboardTranslations.Commands.AddSound.Arguments.Public.name
        description = SoundboardTranslations.Commands.AddSound.Arguments.Public.description
        defaultValue = true
    }

    val volume by optionalInt {
        name = SoundboardTranslations.Commands.AddSound.Arguments.Volume.name
        description = SoundboardTranslations.Commands.AddSound.Arguments.Volume.description
        minValue = 0
        maxValue = 1000
    }
}

fun SubCommandModule.addCommand() = ephemeralSubCommand(::AddSoundCommandArguments) {
    name = SoundboardTranslations.Commands.AddSound.name
    description = SoundboardTranslations.Commands.AddSound.description

    action {
        val found = SoundBoardDatabase.sounds.countDocuments(
            and(Sound::name eq arguments.name, Sound::owner eq user.id)
        )

        if (found >= 1) {
            respond {
                content = translate(SoundboardTranslations.Commands.AddSound.taken)
            }
            return@action
        }

        val id = newId<Sound>()

        @Suppress("INVISIBLE_MEMBER")
        val audioInfo = safeGuild.player.link.loadItem(arguments.sound.url)
        if (audioInfo !is LoadResult.TrackLoaded) {
            respond {
                val message = (audioInfo as? LoadResult.LoadFailed)?.data?.message
                content = translate(SoundboardTranslations.Commands.AddSound.invalidFile, message)
            }
            return@action
        }
        if (audioInfo.data.info.length.toDuration(DurationUnit.MILLISECONDS) > Config.MAX_SOUND_LENGTH) {
            respond {
                content = translate(SoundboardTranslations.Commands.AddSound.tooLong, Config.MAX_SOUND_LENGTH)
            }
            return@action
        }
        val cdnResponse = kord.resources.httpClient.get(arguments.sound.proxyUrl)
        if (!cdnResponse.status.isSuccess()) {
            respond {
                content = translate(SoundboardTranslations.Commands.AddSound.invalidCdnResponse, cdnResponse.status)
            }
            return@action
        }
        val sound = Sound(
            id, arguments.name, user.id,
            arguments.description, arguments.emoji?.toEmoji(),
            public = arguments.public,
            tag = arguments.tag,
            volume = arguments.volume ?: 100
        )
        val file = Config.SOUNDS_FOLDER / sound.fileName
        val soundsFolder = file.parent
        if (!soundsFolder.exists()) {
            soundsFolder.createDirectories()
        }
        file.writeBytes(cdnResponse.body(), StandardOpenOption.CREATE)
        SoundBoardDatabase.sounds.save(sound)

        respond {
            content = translate(SoundboardTranslations.Commands.AddSound.success)
        }

        broadcastEvent(SoundCreatedEvent(sound.convertForNonJvmPlatforms()))
    }
}
