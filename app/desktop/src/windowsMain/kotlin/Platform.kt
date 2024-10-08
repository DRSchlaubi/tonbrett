package dev.schlaubi.tonbrett.app.desktop

import dev.schlaubi.tonbrett.app.desktop.uwp_helper.StringResult
import dev.schlaubi.tonbrett.app.desktop.uwp_helper.UwpHelper.*
import dev.schlaubi.tonbrett.app.uwpTempFolder
import dev.schlaubi.tonbrett.common.Route
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator
import java.net.URI
import kotlin.system.exitProcess

private val LOG = KotlinLogging.logger { }

val currentPlatform: IPlatform = WindowsPlatform()

private class WindowsPlatform : IPlatform {
    override val loginType = Route.Auth.Type.PROTOCOL

    override fun prepareAuthentication(onAuth: () -> Unit): Unit = exitProcess(0)

    @OptIn(DelicateCoroutinesApi::class)
    override fun start(args: Array<String>) {
        val tempFolder = getTempFolder()
        System.setProperty(uwpTempFolder, tempFolder)

        val argsString = args.joinToString(" ")
        if (argsString.startsWith("tonbrett://login")) {
            try {
                LOG.info { "Launched App with $argsString saving token now" }
                val token = Url(argsString).parameters["token"]!!
                setToken(token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            startApplication()
        } else {
            val needsAuth = runCatching { getToken() }.isFailure
            startApplication(needsAuth)
        }
        GlobalScope.launch(Dispatchers.IO) { request_msstore_auto_update() }
    }

    override fun launchUri(uri: URI): Unit = Arena.ofConfined().use { arena ->
        val url = arena.allocateFrom(uri.toString())
        launch_uri(url)
    }

    override fun setToken(token: String) = Arena.ofConfined().use { arena ->
        val tokenStr = arena.allocateFrom(token)
        store_token(tokenStr)
    }

    override fun getToken(): String = invokeStringResultFunction(::get_token)

    private fun getTempFolder(): String = invokeStringResultFunction(::get_temp_folder)

    private fun invokeStringResultFunction(
        function: (SegmentAllocator) -> MemorySegment
    ) = Arena.ofConfined().use { arena ->
        val result = function(arena)
        val isError = StringResult.is_error(result)
        val length = StringResult.length(result).coerceAtLeast(0)
        val buffer = arena.allocate(uint16_t, length)
        copy_string_from_get_string_result_into_buffer(result, buffer)
        val shortArray = buffer.toArray(uint16_t)
        val charArray = CharArray(shortArray.size)
        for ((index, short) in shortArray.withIndex()) {
            charArray[index] = short.toInt().toChar()
        }
        val string = String(charArray)
        if (isError) throw Exception(string) else string
    }
}
