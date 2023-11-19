package dev.schlaubi.tonbrett.app

import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.skiko.SystemTheme
import org.jetbrains.skiko.currentSystemTheme
import kotlin.time.Duration.Companion.seconds

@Composable
actual fun isSystemInDarkMode(): Boolean {
    var darkTheme by remember {
        mutableStateOf(currentSystemTheme == SystemTheme.DARK)
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            darkTheme = currentSystemTheme == SystemTheme.DARK
            delay(1.seconds)
        }
    }

    return darkTheme
}
