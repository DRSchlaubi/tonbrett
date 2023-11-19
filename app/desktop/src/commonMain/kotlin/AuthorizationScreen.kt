package dev.schlaubi.tonbrett.app.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.lyricist.LocalStrings
import dev.schlaubi.tonbrett.app.ColorScheme
import dev.schlaubi.tonbrett.app.api.getUrl
import dev.schlaubi.tonbrett.client.href
import dev.schlaubi.tonbrett.common.Route
import io.ktor.http.*

@Composable
fun AuthorizationScreen(alreadyWaiting: Boolean, onAuth: () -> Unit) {
    var waiting by remember { mutableStateOf(alreadyWaiting) }
    if (waiting) {
        SideEffect {
            launchUri(href(Route.Auth(loginType), URLBuilder(getUrl())).build().toURI())
            prepareAuthentication(onAuth)
        }
    }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().background(ColorScheme.current.container)
    ) {
        val strings = LocalStrings.current
        Text(strings.pleaseSignIn, color = ColorScheme.current.textColor)
        if (waiting) {
            CircularProgressIndicator()
        } else {
            Button({ waiting = true }, contentPadding = PaddingValues(horizontal = 10.dp)) {
                Icon(Icons.Default.OpenInNew, null)
                Text(strings.signInWithDiscord)
            }
        }
    }
}