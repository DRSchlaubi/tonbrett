package dev.schlaubi.tonbrett.app.util

import androidx.compose.runtime.Composable

/**
 * Launches in in-app browser for [url].
 */
@Composable
expect fun InAppBrowser(url: String)
