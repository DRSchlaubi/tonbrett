package dev.schlaubi.tonbrett.app.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import dev.schlaubi.tonbrett.app.api.LocalContext
import platform.Foundation.NSURL
import platform.SafariServices.SFSafariViewController

@Composable
actual fun InAppBrowser(url: String) {
    val context = LocalContext.current
    val safariViewController = remember(url) {
        val nsUrl = NSURL(string = url)
        SFSafariViewController(uRL = nsUrl)
    }

    LaunchedEffect(url) {
        context.present(safariViewController)
    }
}
