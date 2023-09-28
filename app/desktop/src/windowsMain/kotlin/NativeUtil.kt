package dev.schlaubi.tonbrett.app.desktop

import dev.schlaubi.tonbrett.app.desktop.uwp_helper.PasswordVaultResult
import dev.schlaubi.tonbrett.app.desktop.uwp_helper.UwpHelper.*
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.reflect.KFunction

actual val isUwp = true

actual fun launchUri(uri: URI): Unit = Arena.openConfined().use { arena ->
    val url = arena.allocateUtf8String(uri.toString())
    launch_uri(url)
}

actual fun setToken(token: String) = Arena.openConfined().use { arena ->
    val tokenStr = arena.allocateUtf8String(token)
    store_token(tokenStr)
}

actual fun getToken(): String = invokeStringResultFunction(::get_token)

private fun invokeStringResultFunction(
    function: (SegmentAllocator) -> MemorySegment
) =
    Arena.openConfined().use { arena ->
        val result = function(arena)
        val isError = PasswordVaultResult.`is_error$get`(result)
        val length = PasswordVaultResult.`length$get`(result).coerceAtLeast(0)
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
