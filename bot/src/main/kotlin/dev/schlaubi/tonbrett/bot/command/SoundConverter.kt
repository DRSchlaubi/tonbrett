package dev.schlaubi.tonbrett.bot.command

import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.annotations.InternalAPI
import dev.kordex.core.annotations.converters.Converter
import dev.kordex.core.annotations.converters.ConverterType
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.OptionWrapper
import dev.kordex.core.commands.converters.SingleConverter
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.commands.converters.builders.ConverterBuilder
import dev.kordex.core.i18n.EMPTY_KEY
import dev.kordex.core.i18n.types.Key
import dev.kordex.parser.StringParser
import dev.schlaubi.mikbot.plugin.api.util.safeInput
import dev.schlaubi.tonbrett.bot.io.SoundBoardDatabase
import dev.schlaubi.tonbrett.bot.io.search
import dev.schlaubi.tonbrett.bot.translations.SoundboardTranslations
import dev.schlaubi.tonbrett.common.Sound
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

private const val idPrefix = "id:"

@Converter(
    "sound",

    types = [ConverterType.SINGLE]
)
class SoundConverter(validator: Validator<Sound>) : SingleConverter<Sound>(validator) {
    override val signatureType: Key = EMPTY_KEY
    override fun withBuilder(builder: ConverterBuilder<Sound>): SingleConverter<Sound> {
        val builderWithAutoComplete = builder.apply {
            autoComplete { onAutoComplete() }
        }
        return super.withBuilder(builderWithAutoComplete)
    }

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
        val text = parser?.parseNext()?.data ?: return false

        return parseText(text, context)
    }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val optionValue = (option as? StringOptionValue)?.value ?: return false

        return parseText(optionValue, context)
    }

    @Suppress("SameReturnValue") // returns boolean for API sake
    private suspend fun parseText(text: String, context: CommandContext): Boolean {
        if (text.startsWith(idPrefix)) {
            val foundById = SoundBoardDatabase.sounds
                .findOneById(ObjectId(text.substringAfter(idPrefix)))
            if (foundById != null && foundById.owner == context.getUser()?.id) {
                parsed = foundById
                return true
            }

            val foundByName = SoundBoardDatabase.sounds
                .search(text, true, context.getUser()!!.id)
                .firstOrNull()
            if (foundByName != null) {
                parsed = foundByName
            } else {
                handleError(
                    DiscordRelayedException(
                        SoundboardTranslations.Commands.Generic.notFound.withOrdinalPlaceholders(
                            text
                        )
                    ),
                    context
                )
            }
        }

        return true
    }

    @OptIn(InternalAPI::class)
    override suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<*> =
        OptionWrapper(arg.displayName, arg.description, { required = true }, StringChoiceBuilder::class)

    private suspend fun AutoCompleteInteraction.onAutoComplete() {
        val input = focusedOption.safeInput
        val sounds = SoundBoardDatabase.sounds
            .search(input, true, user.id)

        suggestString {
            sounds.take(25).toList().forEach {
                choice(it.name, "id:${it.id}")
            }
        }
    }
}
