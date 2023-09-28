package dev.schlaubi.tonbrett.app.desktop

import java.net.URI
import java.nio.file.Path

/**
 * Whether the current platform is UWP.
 */
expect val isUwp: Boolean

/**
 * Tries to launch the URI using the UWP `Launcher`.
 *
 * @param uri the [URI] to launch
 */
expect fun launchUri(uri: URI)

expect fun getToken(): String

expect fun setToken(token: String)
