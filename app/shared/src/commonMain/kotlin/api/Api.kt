package dev.schlaubi.tonbrett.app.api

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.schlaubi.tonbrett.client.Tonbrett
import io.ktor.http.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers


expect val Dispatchers.IO: CoroutineDispatcher

expect fun getToken(): String
expect fun getUrl(): Url

expect fun reAuthorize()

fun resetApi() {
    api = Tonbrett(getToken(), getUrl())
}

var api by mutableStateOf(Tonbrett(getToken(), getUrl()))
