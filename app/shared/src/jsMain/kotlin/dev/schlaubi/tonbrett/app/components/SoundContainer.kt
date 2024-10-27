package dev.schlaubi.tonbrett.app.components

import io.ktor.http.URLBuilder
import kotlinx.browser.window

val appId = window.location.host.substringBefore(".")

actual fun String.proxyUrl(): String {
    return if (window.location.host.endsWith("discordsays.com")) {
        URLBuilder(this).apply {
            val specifier = when (host) {
                "cdn.discordapp.com" -> "cdn"
                "cdn.jsdelivr.net" -> "jsdelivr"
                else -> error("Unknown host: $host")
            }

            host = "${appId}.discordsays.com"
            pathSegments = listOf(".proxy", specifier) + pathSegments.filter(String::isNotBlank)
        }.buildString()
    } else {
        this
    }
}
