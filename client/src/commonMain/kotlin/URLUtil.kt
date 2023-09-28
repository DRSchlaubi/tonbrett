package dev.schlaubi.tonbrett.client

import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.resources.serialization.*

@PublishedApi
internal val format = ResourcesFormat()

/**
 * Constructs an url for [resource].
 */
inline fun <reified T> href(resource: T) = href(format, resource)

/**
 * Updates the [urlBuilder] to [resource].
 */
inline fun <reified T> href(resource: T, urlBuilder: URLBuilder) = urlBuilder.apply {
    href(format, resource, urlBuilder)
}

/**
 * Constructs an url for [resource] and [baseUrl].
 */
inline fun <reified T> href(resource: T, baseUrl: Url): String = href(resource, URLBuilder(baseUrl)).buildString()
