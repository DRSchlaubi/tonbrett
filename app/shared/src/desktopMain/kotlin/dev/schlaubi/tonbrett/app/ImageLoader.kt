@file:JvmName("ImageLoaderDesktop")
package dev.schlaubi.tonbrett.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState

private val LocalWindow = staticCompositionLocalOf<ComposeWindow> { error("No window") }

@Composable
fun ProvideLocalWindow(windowState: ComposeWindow, content: @Composable () -> Unit) = CompositionLocalProvider(
    LocalWindow provides windowState,
    content = content
)

@Composable
internal actual fun isWindowMinimized(): Boolean = LocalWindow.current.isMinimized
