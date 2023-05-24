package dev.schlaubi.tonbrett.bot.command

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import dev.kord.core.behavior.interaction.suggestString
import dev.schlaubi.mikbot.plugin.api.util.safeInput
import dev.schlaubi.tonbrett.bot.io.SoundBoardDatabase
import dev.schlaubi.tonbrett.bot.io.findAllTags
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList

fun Arguments.tagArgument(name: String, description: String) = optionalString {
    this.name = name
    this.description = description

    autoComplete {
        val input = focusedOption.safeInput
        val tags = SoundBoardDatabase.sounds.findAllTags(input)

        suggestString {
            tags.take(25).toList().forEach {
                choice(it, it)
            }
        }
    }
}
