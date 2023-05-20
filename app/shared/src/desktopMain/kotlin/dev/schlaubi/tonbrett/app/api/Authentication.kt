package dev.schlaubi.tonbrett.app.api

import io.ktor.http.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

var reAuthorize: (() -> Unit)? = null

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual val Dispatchers.IO: CoroutineDispatcher
    get() = IO

actual fun getToken(): String = getConfig().sessionToken ?: error("Please sign in")

actual fun getUrl(): Url = Url("https://schlaubi.eu.ngrok.io")

actual fun reAuthorize() = reAuthorize?.invoke() ?: Unit
