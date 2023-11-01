package dev.schlaubi.tonbrett.app.desktop

import dev.schlaubi.tonbrett.app.desktop.uwp_helper.StringResult
import dev.schlaubi.tonbrett.app.desktop.uwp_helper.UwpHelper.*
import dev.schlaubi.tonbrett.common.Route
import io.ktor.http.*
import mu.KotlinLogging
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator
import java.net.URI
import kotlin.system.exitProcess

private val LOG = KotlinLogging.logger { }

actual val loginType = Route.Auth.Type.PROTOCOL

actual fun prepareAuthentication(onAuth: () -> Unit): Unit = exitProcess(0)

actual fun start(args: Array<String>) {
    val argsString = args.joinToString(" ")
    if (argsString.startsWith("tonbrett://login")) {
        try {
            LOG.info { "Launched App with $argsString saving token now" }
            val token = Url(argsString).parameters["token"]!!
            setToken(token)
        } catch (e: Exception) {
            e.printStackTrace()
            Thread.sleep(50000)
        }
        startApplication()
    } else {
        val needsAuth = runCatching { getToken() }.isFailure
        startApplication(needsAuth)
    }
}

actual fun launchUri(uri: URI): Unit = Arena.ofConfined().use { arena ->
    val url = arena.allocateUtf8String(uri.toString())
    launch_uri(url)
}

actual fun setToken(token: String) = Arena.ofConfined().use { arena ->
    val tokenStr = arena.allocateUtf8String(token)
    store_token(tokenStr)
}

actual fun getToken(): String = invokeStringResultFunction(::get_token)

private fun invokeStringResultFunction(
    function: (SegmentAllocator) -> MemorySegment
) =
    Arena.ofConfined().use { arena ->
        val result = function(arena)
        val isError = StringResult.`is_error$get`(result)
        val length = StringResult.`length$get`(result).coerceAtLeast(0)
        val buffer = arena.allocateArray(uint16_t, length)
        copy_string_from_get_string_result_into_buffer(result, buffer)
        val shortArray = buffer.toArray(uint16_t)
        val charArray = CharArray(shortArray.size)
        for ((index, short) in shortArray.withIndex()) {
            charArray[index] = short.toInt().toChar()
        }
        val string = String(charArray)
        if (isError) throw Exception(string) else string
    }
