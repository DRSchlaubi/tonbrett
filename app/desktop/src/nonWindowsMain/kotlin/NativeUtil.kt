package dev.schlaubi.tonbrett.app.desktop

import java.awt.Desktop
import java.net.URI

actual fun launchUri(uri: URI) {
    val desktop = Desktop.getDesktop()
    if (desktop.isSupported(Desktop.Action.BROWSE)) {
        desktop.browse(uri)
    } else {
        Runtime.getRuntime().exec(arrayOf("xdg-open", uri.toString()))
    }
}

actual fun getAppDataRoaming(): String =
    throw UnsupportedOperationException("This function is only supported on Windows")
