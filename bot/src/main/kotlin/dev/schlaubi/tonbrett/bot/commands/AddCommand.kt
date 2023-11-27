package dev.schlaubi.tonbrett.bot.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.*
import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule
import dev.schlaubi.mikbot.plugin.api.util.kord
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.tonbrett.bot.command.emoji
import dev.schlaubi.tonbrett.bot.command.tagArgument
import dev.schlaubi.tonbrett.bot.command.toEmoji
import dev.schlaubi.tonbrett.bot.config.Config
import dev.schlaubi.tonbrett.bot.io.SoundBoardDatabase
import dev.schlaubi.tonbrett.bot.server.broadcastEvent
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
        name = "attachment"
        description = "commands.add_sound.arguments.attachment.description"
    }

    val name by string {
        name = "name"
        description = "commands.add_sound.arguments.name.description"
        maxLength = NAME_MAX_LENGTH
    }

    val description by optionalString {
        name = "description"
        description = "commands.add_sound.arguments.description.description"
    }

    val emoji by emoji("emoji", "commands.add_sound.arguments.emoji.description")

    val tag by tagArgument("tag", "commands.add_sound.arguments.tag.description")

    val public by defaultingBoolean {
        name = "public"
        description = "commands.add_sound.arguments.public.description"
        defaultValue = true
    }

    val volume by optionalInt {
        name = "volume"
        description = "commands.add_sound.arguments.volume.description"
        minValue = 0
        maxValue = 1000
    }
}

fun SubCommandModule.addCommand() = ephemeralSubCommand(::AddSoundCommandArguments) {
    name = "add"
    description = "commands.add_sound.description"

    action {
        val found = SoundBoardDatabase.sounds.countDocuments(
            and(Sound::name eq arguments.name, Sound::owner eq user.id)
        )

        if (found >= 1) {
            respond {
                content = translate("commands.add_sound.taken")
            }
            return@action
        }

        val id = newId<Sound>()

        @Suppress("INVISIBLE_MEMBER")
        val audioInfo = safeGuild.player.link.loadItem(arguments.sound.url)
        if (audioInfo !is LoadResult.TrackLoaded) {
            respond {
                val message = (audioInfo as? LoadResult.LoadFailed)?.data?.message
                content = translate("commands.add_sound.invalid_file", arrayOf(message))
            }
            return@action
        }
        if (audioInfo.data.info.length.toDuration(DurationUnit.MILLISECONDS) > Config.MAX_SOUND_LENGTH) {
            respond {
                content = translate("commands.add_sound.too_long", arrayOf(Config.MAX_SOUND_LENGTH))
            }
            return@action
        }
        val cdnResponse = kord.resources.httpClient.get(arguments.sound.proxyUrl)
        if (!cdnResponse.status.isSuccess()) {
            respond {
                content = translate("commands.add_sound.invalid_cdn_response", arrayOf(cdnResponse.status))
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
            content = translate("commands.add_sound.success")
        }

        broadcastEvent(SoundCreatedEvent(sound.convertForNonJvmPlatforms()))
    }
}
