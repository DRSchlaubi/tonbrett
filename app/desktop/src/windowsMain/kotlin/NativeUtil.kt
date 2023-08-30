package dev.schlaubi.tonbrett.app.desktop

import dev.schlaubi.tonbrett.app.desktop.uwp_helper.AppDataRoamingResult
import dev.schlaubi.tonbrett.app.desktop.uwp_helper.UwpHelper.*
import java.lang.foreign.Arena
import java.net.URI

actual fun launchUri(uri: URI): Unit = Arena.openConfined().use { arena ->
    val url = arena.allocateUtf8String(uri.toString())
    launch_uri(url)
}

actual fun getAppDataRoaming(): String =
    Arena.openConfined().use { arena ->
        val result = get_app_data_roaming(arena)
        val isError = AppDataRoamingResult.`is_error$get`(result)
        val length = AppDataRoamingResult.`length$get`(result).coerceAtLeast(0)
        val buffer = arena.allocateArray(uint16_t, length)
        copy_string_from_get_app_data_roaming_result_into_buffer(result, buffer)
        val shortArray = buffer.toArray(uint16_t)
        val charArray = CharArray(shortArray.size)
        for ((index, short) in shortArray.withIndex()) {
            charArray[index] = short.toInt().toChar()
        }
        val string = String(charArray)
        if (isError) throw Exception(string) else string
    }
