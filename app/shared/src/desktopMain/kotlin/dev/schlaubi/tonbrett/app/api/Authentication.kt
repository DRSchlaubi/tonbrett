package dev.schlaubi.tonbrett.app.api

import io.ktor.http.*

var reAuthorize: (() -> Unit)? = null

actual fun getToken(): String = getConfig().sessionToken ?: error("Please sign in")

actual fun getUrl(): Url = Url("https://schlaubi.eu.ngrok.io")

actual fun reAuthorize() = reAuthorize?.invoke() ?: Unit
