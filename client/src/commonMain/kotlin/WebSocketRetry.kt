@file:Suppress("INVISIBLE_MEMBER", "NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")

package dev.schlaubi.tonbrett.client

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.api.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.utils.io.core.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import mu.KotlinLogging
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val LOG = KotlinLogging.logger {  }

/**
 * WebSocket version of the [HttpRequestRetry] plugin.
 *
 * @see retryingWebSocket
 */
val WebSocketRetry = createClientPlugin("WebSocketRetry", { HttpRequestRetry.Configuration() }) {}

/**
 * Exception used for logging WebSocket connection errors.
 *
 * @param protocolCause the [CloseReason] if received.
 */
class DisconnectedException(protocolCause: CloseReason?) :
    IllegalStateException("Got disconnected from WebSocket for: $protocolCause")

/**
 * This works identical to [webSocket] but retries if connection errors occur.
 */
suspend fun HttpClient.retryingWebSocket(
    httpRequestBuilder: HttpRequestBuilder.() -> Unit,
    handler: suspend DefaultClientWebSocketSession.() -> Unit
) {
    val plugin = plugin(WebSocketRetry)
    val context = WebSocketRetryContext(this, plugin.config, httpRequestBuilder, handler)
    context.connect()
}

private class WebSocketRetryContext(
    val client: HttpClient,
    val config: HttpRequestRetry.Configuration,
    val httpRequestBuilder: HttpRequestBuilder.() -> Unit,
    val handler: suspend DefaultClientWebSocketSession.() -> Unit
) {
    lateinit var session: DefaultClientWebSocketSession
    private var delayContext: HttpRequestRetry.DelayContext? = null
    private var tries = 1

    fun reset() {
        if (delayContext != null) {
            LOG.info { "Successfully reconnected to: ${session.call.request.url}" }
        }
        delayContext = null
        tries = 1
    }

    suspend fun reconnect(e: Throwable, isRetry: Boolean = true) {
        if (!isRetry) {
            delayContext = HttpRequestRetry.DelayContext(
                HttpRequestBuilder(),
                session.call.response,
                e
            )
        } else if (++tries >= config.maxRetries) {
            return
        }
        val context = delayContext ?: error("Missing delay context")

        val delayMillis = config.delayMillis.invoke(context, tries)
        LOG.warn { "Retry $tries/${config.maxRetries}. Waiting ${delayMillis.toDuration(DurationUnit.MILLISECONDS)}" }
        config.delay.invoke(delayMillis)
        connect()
    }

    suspend fun connect() {
        suspend fun retry(e: Throwable) {
            val reason = session.closeReason.await()
            if (reason?.knownReason == CloseReason.Codes.VIOLATED_POLICY) return run { println("ERE") }
            val exception = DisconnectedException(reason)
            LOG.warn(exception) { "Got disconnected from WebSocket" }
            reconnect(e, isRetry = false)
        }

        try {
            session = try {
                client.webSocketSession(httpRequestBuilder)
            } catch (e: Throwable) {
                reconnect(e, delayContext != null)
                return
            }
            reset()

            handler(session)
            reconnect(IllegalStateException("Got disconnected"), isRetry = false)
        } catch (e: ClosedReceiveChannelException) {
            retry(e)
        } catch (e: EOFException) {
            retry(e)
        }
    }
}
