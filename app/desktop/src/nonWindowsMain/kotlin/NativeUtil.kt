package dev.schlaubi.tonbrett.app.desktop

import java.awt.Desktop
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div

actual val isUwp = false

actual fun launchUri(uri: URI) {
    val desktop = Desktop.getDesktop()
    if (desktop.isSupported(Desktop.Action.BROWSE)) {
        desktop.browse(uri)
    } else {
        Runtime.getRuntime().exec(arrayOf("xdg-open", uri.toString()))
    }
}

actual fun getAppDataRoaming(): Path {
    val os = System.getProperty("os.name")
    val basePath = when {
        os.contains("windows", ignoreCase = true) -> Path(System.getenv("APPDATA"))
        os.contains("mac", ignoreCase = true) ->
            Path(System.getenv("HOME")) / "Library" / "Application Support"
        else -> Path(System.getProperty("user.home"))
    }
    return basePath / "Tonbrett"
}
