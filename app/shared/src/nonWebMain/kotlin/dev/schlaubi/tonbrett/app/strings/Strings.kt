package dev.schlaubi.tonbrett.app.strings

import androidx.compose.ui.text.intl.Locale

actual fun getLanguageTag(): String = Locale.current.toLanguageTag()
