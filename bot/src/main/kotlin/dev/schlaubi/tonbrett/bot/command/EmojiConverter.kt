package dev.schlaubi.tonbrett.bot.command

import com.kotlindiscord.kord.extensions.annotations.UnexpectedFunctionBehaviour
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.Converter
import com.kotlindiscord.kord.extensions.commands.converters.ConverterToOptional
import com.kotlindiscord.kord.extensions.commands.converters.OptionalCoalescingConverter
import com.kotlindiscord.kord.extensions.commands.converters.builders.ConverterBuilder
import com.kotlindiscord.kord.extensions.commands.converters.builders.OptionalCoalescingConverterBuilder
import com.kotlindiscord.kord.extensions.commands.converters.impl.emoji
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.converters.union
import dev.kord.core.entity.GuildEmoji
import dev.kord.core.entity.StandardEmoji
import dev.kord.x.emoji.Emojis
import dev.schlaubi.tonbrett.common.Sound

@OptIn(UnsafeAPI::class, ConverterToOptional::class, UnexpectedFunctionBehaviour::class)
fun Arguments.emoji(name: String, description: String): OptionalCoalescingConverter<Any> {
    fun ConverterBuilder<*>.applyName() {
        this.name = name
        this.description = description
    }
    val emoji = emoji {
        applyName()
    }
    val string = string {
        applyName()

        validate {
            if (Emojis[value] == null) {
                fail(translate("arguments.emoji.invalid"))
            }
        }
    }

    val brokenConverter =  union(name, description, converters = arrayOf(emoji, string))
    args.removeIf { it.converter == brokenConverter }
    return arg(name, description, brokenConverter.toOptional().withBuilder(DummyBuilder))
}

context(CommandContext)
fun Any.toEmoji(): Sound.Emoji = when (this) {
    is String -> Sound.DiscordEmoji(this)
    is GuildEmoji -> Sound.GuildEmoji(id, isAnimated)
    is StandardEmoji -> Sound.DiscordEmoji(name)
    else -> error("Invalid emoji type: $this")
}

object DummyBuilder : OptionalCoalescingConverterBuilder<Any>() {
    override fun build(arguments: Arguments): Converter<*, *, *, *> {
        throw UnsupportedOperationException("Not supported by dummy")
    }
}
