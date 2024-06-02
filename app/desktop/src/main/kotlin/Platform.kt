@file:JvmName("Platform")
package dev.schlaubi.tonbrett.app.desktop

import dev.schlaubi.tonbrett.common.Route
import java.net.URI

/**
 * Implementations are in `src/nonWindowsMain` and `src/windowsMain`
 */
object Platform : IPlatform by currentPlatform

interface IPlatform {
    val loginType: Route.Auth.Type

    /**
     * Runs any preparation steps for authentication
     */
    fun prepareAuthentication(onAuth: () -> Unit)

    fun start(args: Array<String>)

    /**
     * Tries to launch the URI using the UWP `Launcher`.
     *
     * @param uri the [URI] to launch
     */
    fun launchUri(uri: URI)

    fun getToken(): String

    fun setToken(token: String)
}
