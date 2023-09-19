package dev.schlaubi.tonbrett.app.web.resource

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.CanvasBasedWindow
import dev.schlaubi.tonbrett.app.title
import dev.schlaubi.tonbrett.common.TonbrettSerializersModule
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.resources.serialization.*
import kotlinx.browser.window
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.serializer
import org.jetbrains.skiko.wasm.onWasmReady

@PublishedApi
internal val format = ResourcesFormat(TonbrettSerializersModule)

/**
 * Creates a route handler for the [R] resource.
 */
@OptIn(ExperimentalSerializationApi::class)
inline fun <reified R : Any> routeHandler(body: (R) -> Unit) {
    val serializer = format.serializersModule.serializer<R>()
    check(serializer.descriptor.annotations.any { it is Resource }) {
        "Specified type must be resource"
    }
    val pattern = format.encodeToPathPattern(serializer)
    if (window.location.pathname.replaceInitialSlash() != pattern) return

    val parameters = window.location.search.parseUrlEncodedParameters()
    val resources = format.decodeFromParameters(serializer, parameters)

    body(resources)
}

/**
 * Creates a route handler for the [R] resource, which can use [Composable] functions
 *
 * @see routeHandler
 */
@Suppress("FunctionName")
inline fun <reified R : Any> ComposeRouteHandler(crossinline content: @Composable (R) -> Unit) = routeHandler<R> { resource ->
    onWasmReady {
        CanvasBasedWindow(title) {
            content(resource)
        }
    }
}

@PublishedApi
internal fun String.replaceInitialSlash() = replaceFirstChar {
    if (it == '/') {
        ""
    } else {
        it.toString()
    }
}