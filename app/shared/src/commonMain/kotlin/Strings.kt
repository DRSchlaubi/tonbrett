package dev.schlaubi.tonbrett.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.intl.Locale
import cafe.adriel.lyricist.Lyricist
import cafe.adriel.lyricist.LanguageTag
import cafe.adriel.lyricist.rememberStrings
import cafe.adriel.lyricist.ProvideStrings
import dev.schlaubi.tonbrett.app.strings.DeStrings
import dev.schlaubi.tonbrett.app.strings.EnStrings
import dev.schlaubi.tonbrett.app.strings.Strings

public val Strings: Map<LanguageTag, Strings> = mapOf(
    "de" to DeStrings,
    "en" to EnStrings
)

public val LocalStrings: ProvidableCompositionLocal<Strings> = 
    staticCompositionLocalOf { EnStrings }



@Composable
public fun rememberStrings(
    defaultLanguageTag: LanguageTag = "en",
    currentLanguageTag: LanguageTag = Locale.current.toLanguageTag(),
): Lyricist<Strings> =
    rememberStrings(Strings, defaultLanguageTag, currentLanguageTag)

@Composable
public fun ProvideStrings(
    lyricist: Lyricist<Strings> = rememberStrings(),
    content: @Composable () -> Unit
) {
    ProvideStrings(lyricist, LocalStrings, content)
}