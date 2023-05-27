package dev.schlaubi.tonbrett.app.util

import kotlinx.browser.window

private val regex = "Android|iPhone".toRegex(RegexOption.IGNORE_CASE)

actual val isMobile: Boolean = regex.find(window.navigator.userAgent) != null
