package dev.schlaubi.tonbrett.app.web

import androidx.compose.ui.window.CanvasBasedWindow
import dev.schlaubi.tonbrett.app.TonbrettApp
import dev.schlaubi.tonbrett.app.title

fun main() {
    CanvasBasedWindow(title) {
        TonbrettApp()
    }
}
