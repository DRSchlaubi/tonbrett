package dev.schlaubi.tonbrett.app.desktop

import dev.schlaubi.tonbrett.common.Route
import java.awt.Desktop
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div

val currentPlatform: IPlatform = UnixPlatform()

private class UnixPlatform : IPlatform {

    override val loginType = Route.Auth.Type.APP

    override fun start(args: Array<String>) {
        val needsAuth = runCatching { getToken() }.isFailure
        startApplication(needsAuth)
    }

    override fun prepareAuthentication(onAuth: () -> Unit) = startAuthorizationServer(onAuth)

    override fun launchUri(uri: URI) {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(uri)
        } else {
            ProcessBuilder("xdg-open", uri.toString()).apply {
                inheritIO()
            }.start()
        }
    }

    override fun getToken() = getConfig().sessionToken ?: error("Not signed in")
    override fun setToken(token: String) = saveConfig(Config((token)))
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
