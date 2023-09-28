package dev.schlaubi.tonbrett.app.desktop

import dev.schlaubi.tonbrett.common.Route
import java.awt.Desktop
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div

actual val loginType = Route.Auth.Type.APP

actual fun start(args: Array<String>) {
    val needsAuth = runCatching { getToken() }.isFailure
    startApplication(needsAuth)
}

actual fun prepareAuthentication(onAuth: () -> Unit) = startAuthorizationServer(onAuth)

actual fun launchUri(uri: URI) {
    val desktop = Desktop.getDesktop()
    if (desktop.isSupported(Desktop.Action.BROWSE)) {
        desktop.browse(uri)
    } else {
        Runtime.getRuntime().exec(arrayOf("xdg-open", uri.toString()))
    }
}

fun getAppDataRoaming(): Path {
    val os = System.getProperty("os.name")
    val basePath = when {
        os.contains("windows", ignoreCase = true) -> Path(System.getenv("APPDATA"))
        os.contains("mac", ignoreCase = true) ->
            Path(System.getenv("HOME")) / "Library" / "Application Support"
        else -> Path(System.getProperty("user.home"))
    }
    return basePath / "Tonbrett"
}

actual fun getToken() = getConfig().sessionToken ?: error("Not signed in")
actual fun setToken(token: String) = saveConfig(Config((token)))
