/**
 * The generator uses a file called `LyricistUtils.kt` which for some reason doesn't seem to exist at runtime.
 * Also `Locale.toLanguageTag()` is not implemented on js
 */
package dev.schlaubi.tonbrett.app.strings

import androidx.compose.runtime.*
import cafe.adriel.lyricist.LanguageTag
import cafe.adriel.lyricist.Lyricist

expect fun getLanguageTag(): String

private val StringsMap: Map<LanguageTag, Strings> = mapOf(
    "de" to DeStrings,
    "en" to EnStrings
)

val LocalStrings: ProvidableCompositionLocal<Strings> = compositionLocalOf { EnStrings }

@Composable
fun rememberStrings(
    languageTag: LanguageTag = getLanguageTag()
): Lyricist<Strings> =
    remember {
        Lyricist("en", StringsMap).apply {
            this.languageTag = languageTag
        }
    }

@Composable
fun ProvideStrings(
    lyricist: Lyricist<Strings> = rememberStrings(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalStrings provides lyricist.strings,
        content = content
    )
}
