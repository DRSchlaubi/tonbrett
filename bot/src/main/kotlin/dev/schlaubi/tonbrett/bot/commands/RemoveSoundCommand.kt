 package dev.schlaubi.tonbrett.bot.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule
import dev.schlaubi.tonbrett.bot.command.sound
import dev.schlaubi.tonbrett.bot.io.SoundBoardDatabase
import dev.schlaubi.tonbrett.bot.server.broadcastEvent
import dev.schlaubi.tonbrett.common.SoundDeletedEvent

 class RemoveSoundArguments : Arguments() {
    val sound by sound {
        name = "sound"
        description = "commands.remove_sound.arguments.sound.description"
    }
}

fun SubCommandModule.removeCommand() = ephemeralSubCommand(::RemoveSoundArguments) {
    name = "remove"
    description = "commands.remove.description"

    action {
        SoundBoardDatabase.sounds.deleteOneById(arguments.sound.id)

        respond {
            content = translate("commands.remove_sound.success")
        }

        broadcastEvent(SoundDeletedEvent(arguments.sound.id))
    }
}
