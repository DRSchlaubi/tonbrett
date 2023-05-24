package dev.schlaubi.tonbrett.bot.command

import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.commands.converters.builders.ConverterBuilder
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.safeInput
import dev.schlaubi.tonbrett.bot.io.SoundBoardDatabase
import dev.schlaubi.tonbrett.bot.io.search
import dev.schlaubi.tonbrett.common.Sound
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.litote.kmongo.and
import org.litote.kmongo.eq

private const val idPrefix = "id:"

@Converter(
    "sound",

    types = [ConverterType.SINGLE]
)
class SoundConverter(validator: Validator<Sound>) : SingleConverter<Sound>(validator) {
    override val signatureTypeString: String = "Sound"
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
                discordError(context.translate("commands.generic.not_found", arrayOf(text)))
            }
        }

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

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
