package dev.schlaubi.tonbrett.app.desktop

import dev.schlaubi.tonbrett.app.desktop.uwp_helper.AppDataRoamingResult.`is_error$get`
import dev.schlaubi.tonbrett.app.desktop.uwp_helper.AppDataRoamingResult.`length$get`
import dev.schlaubi.tonbrett.app.desktop.uwp_helper.UwpHelper.*
import java.lang.foreign.Arena
import java.net.URI

/**
 * Tries to launch the URI using the UWP `Launcher`.
 *
 * @param uri the [URI] to launch
 */
fun launchUri(uri: URI) = Arena.openConfined().use { arena ->
    val url = arena.allocateUtf8String(uri.toString())
    launch_uri(url)
}

/**
 * Tries to retrieve the current UWP app data folder.
 *
 * @return the absolute path to the folder
 */
fun getAppDataRoaming() = Arena.openConfined().use { arena ->
    val result = get_app_data_roaming(arena)
    val isError = `is_error$get`(result)
    val length = `length$get`(result).coerceAtLeast(0)
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
