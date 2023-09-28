@file:JvmName("Platform")
package dev.schlaubi.tonbrett.app.desktop

import dev.schlaubi.tonbrett.common.Route
import java.net.URI

expect val loginType: Route.Auth.Type

/**
 * Runs any preparation steps for authentication
 */
expect fun prepareAuthentication(onAuth: () -> Unit)

expect fun start(args: Array<String>)

/**
 * Tries to launch the URI using the UWP `Launcher`.
 *
 * @param uri the [URI] to launch
 */
expect fun launchUri(uri: URI)

expect fun getToken(): String

expect fun setToken(token: String)