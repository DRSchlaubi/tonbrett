package dev.schlaubi.tonbrett.app.api

import io.ktor.http.*
import kotlinx.browser.window
import org.w3c.dom.get

const val tokenKey = "token"

actual fun getToken() = window.localStorage[tokenKey] ?: error("Please sign in")

actual fun getUrl() = URLBuilder(window.location.href).apply {
    pathSegments = emptyList()
}.build()
