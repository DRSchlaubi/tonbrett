package dev.schlaubi.tonbrett.bot.command

import dev.kord.core.entity.GuildEmoji
import dev.kord.core.entity.StandardEmoji
import dev.kord.x.emoji.Emojis
import dev.kordex.core.annotations.UnexpectedFunctionBehaviour
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.Converter
import dev.kordex.core.commands.converters.ConverterToOptional
import dev.kordex.core.commands.converters.OptionalCoalescingConverter
import dev.kordex.core.commands.converters.builders.ConverterBuilder
import dev.kordex.core.commands.converters.builders.OptionalCoalescingConverterBuilder
import dev.kordex.core.commands.converters.impl.emoji
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.i18n.EMPTY_KEY
import dev.kordex.core.i18n.types.Key
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.converters.union
import dev.schlaubi.tonbrett.bot.translations.SoundboardTranslations
import dev.schlaubi.tonbrett.common.Sound

@OptIn(UnsafeAPI::class, ConverterToOptional::class, UnexpectedFunctionBehaviour::class)
fun Arguments.emoji(name: Key, description: Key): OptionalCoalescingConverter<Any> {
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
                fail(SoundboardTranslations.Arguments.Emoji.invalid)
            }
        }
    }

    val brokenConverter = union(name, description, typeName = EMPTY_KEY, converters = arrayOf(emoji, string))
    args.removeIf { it.converter == brokenConverter }
    return arg(name, description, brokenConverter.toOptional().withBuilder(DummyBuilder))
}

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
