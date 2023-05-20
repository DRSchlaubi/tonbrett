package dev.schlaubi.tonbrett.app.api

import dev.schlaubi.tonbrett.app.shared.BuildConfig
import io.ktor.http.*

actual fun getUrl(): Url = Url(BuildConfig.API_URL)
