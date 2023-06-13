package dev.schlaubi.tonbrett.app.desktop

import java.awt.Desktop
import java.net.URI

fun browseUrl(url: URI) {
    val desktop = Desktop.getDesktop()
    if (desktop.isSupported(Desktop.Action.BROWSE)) {
        desktop.browse(url)
    } else {
        Runtime.getRuntime().exec(arrayOf("xdg-open", url.toString()))
    }
}
