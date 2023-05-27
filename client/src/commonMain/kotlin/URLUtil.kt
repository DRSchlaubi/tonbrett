package dev.schlaubi.tonbrett.client

import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.resources.serialization.*

val format = ResourcesFormat()
inline fun <reified T> href(resource: T) = href(format, resource)
inline fun <reified T> href(resource: T, urlBuilder: URLBuilder) = urlBuilder.apply {
    href(format, resource, urlBuilder)
}
inline fun <reified T> href(resource: T, baseUrl: Url): String = href(resource, URLBuilder(baseUrl)).buildString()
