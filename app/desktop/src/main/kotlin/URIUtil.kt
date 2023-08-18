package dev.schlaubi.tonbrett.app.desktop

import java.awt.Desktop
import java.net.URI

fun browseUrl(url: URI) {
    if (System.getProperty("os.name").contains("windows", ignoreCase = true) && windowsAppDataFolder != null) {
        NativeUtil.launchUri(url)
    } else {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(url)
        } else {
            Runtime.getRuntime().exec(arrayOf("xdg-open", url.toString()))
        }
    }
}
