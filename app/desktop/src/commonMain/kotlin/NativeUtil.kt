package dev.schlaubi.tonbrett.app.desktop

import java.net.URI

/**
 * Tries to launch the URI using the UWP `Launcher`.
 *
 * @param uri the [URI] to launch
 */
expect fun launchUri(uri: URI)

/**
 * Tries to retrieve the current UWP app data folder.
 *
 * @return the absolute path to the folder
 */
expect fun getAppDataRoaming(): String