package dev.schlaubi.tonbrett.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.lyricist.LocalStrings
import dev.schlaubi.tonbrett.app.api.LocalContext
import dev.schlaubi.tonbrett.app.api.getUrl
import dev.schlaubi.tonbrett.app.util.InAppBrowser
import dev.schlaubi.tonbrett.client.href
import dev.schlaubi.tonbrett.common.Route

@Composable
fun MobileTonbrettApp(receivedToken: String? = null) {
    val sessionExpired = remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showLogin by remember { mutableStateOf(false) }
    if (showLogin) {
        showLogin = false
        val url = href(Route.Auth(Route.Auth.Type.MOBILE_APP), getUrl())
        InAppBrowser(url)
    } else if (receivedToken != null || context.isSignedIn || sessionExpired.value) {
        if (receivedToken != null) {
            sessionExpired.value = false
            context.token = receivedToken
        }
        context.resetApi()

        SideEffect {
            context.withReAuthroize {
                showLogin = true
            }
        }
        TonbrettApp(sessionExpired)
    } else {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            val strings = LocalStrings.current
            if (sessionExpired.value) {
                Text(strings.sessionExpiredExplainer)
            } else {
                Text(strings.pleaseSignIn)
            }
            Button({
                showLogin = true
            }) {
                Icon(Icons.Default.OpenInBrowser, null)
                Text(strings.signInWithDiscord)
            }
        }
    }
}