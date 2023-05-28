package dev.schlaubi.tonbrett.app.web

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.schlaubi.tonbrett.app.ColorScheme
import dev.schlaubi.tonbrett.app.strings.LocalStrings
import dev.schlaubi.tonbrett.common.authServerPort
import io.ktor.client.fetch.*
import kotlinx.browser.window
import kotlinx.coroutines.await
import mu.KotlinLogging
import org.w3c.dom.url.URLSearchParams

private val LOG = KotlinLogging.logger { }

@Composable
fun AuthorizationScreen() {
    val strings = LocalStrings.current
    LaunchedEffect(Unit) {
        val token = URLSearchParams(window.location.search).get("token") ?: error("Missing token")
        try {
            fetch("http://localhost:$authServerPort/login?token=$token", object : RequestInit {
                override var method: String? = "POST"
            }).await()
        } catch (e: Throwable) {
            LOG.error(e) { "Could not propagate token" }
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(ColorScheme.container)) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.background(ColorScheme.searchBarColor, shape = RoundedCornerShape(10.dp))
                    .wrapContentSize().sizeIn(minWidth = 108.dp, minHeight = 256.dp, maxWidth = 512.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    ProvideTextStyle(TextStyle(textAlign = TextAlign.Center, color = ColorScheme.textColor)) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            null,
                            tint = Color.Green,
                            modifier = Modifier.padding(vertical = 5.dp).size(64.dp)
                        )
                        Text(strings.loginSuccessful, fontSize = 64.sp)
                        Spacer(Modifier.padding(vertical = 15.dp))
                        Text(strings.logo, modifier = Modifier.width(64.dp), fontSize = 16.sp)
                        Spacer(Modifier.padding(vertical = 7.dp))
                        Text(
                            strings.loginSuccessfulDescription,
                            fontSize = 24.sp
                        )
                        Divider(Modifier.padding(vertical = 15.dp).fillMaxWidth(), thickness = 2.dp)
                        Text(
                            strings.starOnGithub,
                            fontSize = 24.sp,
                            color = Color.Blue,
                            modifier = Modifier.clickable {
                                window.location.href = "https://github.com/DRSchlaubi/Tonbrett"
                            })
                    }
                }
            }
        }
    }
}
