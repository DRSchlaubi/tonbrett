package dev.schlaubi.tonbrett.app.web

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.schlaubi.tonbrett.app.LocalStrings
import dev.schlaubi.tonbrett.app.ColorScheme
import dev.schlaubi.tonbrett.common.authServerPort
import io.ktor.client.fetch.*
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import io.github.oshai.kotlinlogging.KotlinLogging
import org.w3c.dom.url.URLSearchParams
import kotlin.time.Duration.Companion.seconds

private val LOG = KotlinLogging.logger { }

@Composable
fun AuthorizationScreen(cli: Boolean, protocol: Boolean) {
    val strings = LocalStrings.current
    val token =
        remember { URLSearchParams(window.location.search).get("token") ?: error("Missing token") }
    if (!cli) {
        LaunchedEffect(Unit) {
            try {
                if (protocol) {
                    window.location.href = "tonbrett://login?token=$token"
                } else {
                    fetch(
                        "http://localhost:$authServerPort/login?token=$token",
                        object : RequestInit {
                            override var method: String? = "POST"
                        }).await()
                }
            } catch (e: Throwable) {
                LOG.error(e) { "Could not propagate token" }
            }
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(ColorScheme.current.container)) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.background(
                    ColorScheme.current.searchBarColor,
                    shape = RoundedCornerShape(10.dp)
                )
                    .wrapContentSize()
                    .sizeIn(minWidth = 108.dp, minHeight = 256.dp, maxWidth = 512.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    ProvideTextStyle(
                        TextStyle(
                            textAlign = TextAlign.Center,
                            color = ColorScheme.current.textColor
                        )
                    ) {
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
                        val text = if (cli) {
                            strings.cliLoginExplainer
                        } else {
                            strings.loginSuccessfulDescription
                        }
                        Text(text, fontSize = 24.sp)
                        Spacer(Modifier.padding(vertical = 7.dp))
                        if (cli) {
                            BoxWithConstraints(
                                Modifier.background(ColorScheme.current.container, RoundedCornerShape(4.dp))
                                    .fillMaxWidth()
                                    .height(32.dp)
                                    .padding(horizontal = 15.dp)
                            ) {
                                val command = "tonbrett-cli login --auth-token=$token"
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    SelectionContainer {
                                        Text(
                                            command,
                                            maxLines = 1,
                                            modifier = Modifier.horizontalScroll(rememberScrollState())
                                        )
                                    }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.zIndex(2f).fillMaxWidth()
                                ) {
                                    CopyButton(command)
                                }
                            }
                        }
                        Divider(Modifier.padding(vertical = 15.dp).fillMaxWidth(), thickness = 2.dp)
                        Text(
                            strings.starOnGithub,
                            fontSize = 24.sp,
                            color = Color.Blue,
                            modifier = Modifier.clickable {
                                window.location.href = "https://github.com/DRSchlaubi/Tonbrett"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CopyButton(text: String) {
    var copied by remember { mutableStateOf(false) }

    if (copied) {
        LaunchedEffect(copied) {
            delay(3.seconds)
            copied = false
        }
    }

    Surface(
        color = ColorScheme.current.secondaryContainer, shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 3.dp)
    ) {
        IconButton(
            {
                window.navigator.clipboard.writeText(text)
                copied = true
            }
        ) {
            val icon = if (copied) {
                Icons.Default.Check
            } else {
                Icons.Default.ContentCopy
            }
            Icon(icon, null, tint = ColorScheme.current.textColor)
        }
    }
}
