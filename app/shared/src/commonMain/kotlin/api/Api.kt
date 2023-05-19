package dev.schlaubi.tonbrett.app.api

import dev.schlaubi.tonbrett.client.Tonbrett
import io.ktor.http.*

expect fun getToken(): String
expect fun getUrl(): Url

//expect fun getLanguageTag(): String

val api = Tonbrett(getToken(), getUrl())
