package dev.schlaubi.tonbrett.app.util

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import dev.schlaubi.tonbrett.app.api.LocalContext

@Composable
actual fun InAppBrowser(url: String) {
    val context = LocalContext.current.currentActivity
    val intent = remember(url) { CustomTabsIntent.Builder().build() }

    LaunchedEffect(url) {
        intent.launchUrl(context, Uri.parse(url))
    }
}
