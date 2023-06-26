package dev.schlaubi.tonbrett.app.desktop

import java.awt.Desktop
import java.net.URI

fun browseUrl(url: URI) {
    // Desktop#browse() internally uses ShellExecute, which doesn't work in UWP apps, therefore,
    // We use a Rust binary, which calls:
    // https://learn.microsoft.com/de-de/uwp/api/windows.system.launcher.launchuriasync?view=winrt-22621
    if (System.getProperty("os.name").contains("windows", ignoreCase = true)) {
        Runtime.getRuntime().exec(arrayOf("url_launcher.exe", "-u", url.toString()))
    } else {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(url)
        } else {
            Runtime.getRuntime().exec(arrayOf("xdg-open", url.toString()))
        }
    }
}
