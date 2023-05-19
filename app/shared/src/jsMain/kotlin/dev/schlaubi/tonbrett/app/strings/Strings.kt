package dev.schlaubi.tonbrett.app.strings

import kotlinx.browser.window

actual fun getLanguageTag(): String = window.navigator.language
