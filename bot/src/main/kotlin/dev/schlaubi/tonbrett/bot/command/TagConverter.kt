package dev.schlaubi.tonbrett.bot.command

import dev.kord.core.behavior.interaction.suggestString
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.optionalString
import dev.kordex.core.i18n.types.Key
import dev.schlaubi.mikbot.plugin.api.util.safeInput
import dev.schlaubi.tonbrett.bot.io.SoundBoardDatabase
import dev.schlaubi.tonbrett.bot.io.findAllTags
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList

fun Arguments.tagArgument(name: Key, description: Key) = optionalString {
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
