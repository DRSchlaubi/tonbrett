package dev.schlaubi.tonbrett.bot.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.attachment
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalEmoji
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.plugin.api.util.kord
import dev.schlaubi.tonbrett.bot.config.Config
import dev.schlaubi.tonbrett.bot.io.SoundBoardDatabase
import dev.schlaubi.tonbrett.common.Sound
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.litote.kmongo.newId
import java.nio.file.StandardOpenOption
import kotlin.io.path.absolutePathString
import kotlin.io.path.div
import kotlin.io.path.writeBytes

class AddSoundCommandArgs : Arguments() {
    val sound by attachment {
        name = "attachment"
        description = "commands.add_sound.arguments.attachment.description"
    }

    val name by string {
        name = "name"
        description = "commands.add_sound.arguments.name.description"
    }

    val description by optionalString {
        name = "description"
        description = "commands.add_sound.arguments.description.description"
    }

    val emoji by optionalEmoji {
        name = "emoji"
        description = "commands.add_sound.arguments.emoji.description"
    }
}

suspend fun Extension.addSoundCommand() = ephemeralSlashCommand(::AddSoundCommandArgs) {
    name = "add-sound"
    description = "commands.add_sound.description"

    action {
        val id = newId<Sound>()
        val cdnResponse = kord.resources.httpClient.get(arguments.sound.proxyUrl)
        if (!cdnResponse.status.isSuccess()) {
            respond {
                content = translate("commands.add_sound.invalid_cdn_response", arrayOf(cdnResponse.status))
            }
            return@action
        }
        val sound = Sound(id, arguments.name, user.id, arguments.description, null)
        val file = Config.SOUNDS_FOLDER / sound.fileName
        file.writeBytes(cdnResponse.body(), StandardOpenOption.CREATE)
        SoundBoardDatabase.sounds.save(sound)

        respond {
            content = translate("commands.add_sound.success")
        }
    }
}
