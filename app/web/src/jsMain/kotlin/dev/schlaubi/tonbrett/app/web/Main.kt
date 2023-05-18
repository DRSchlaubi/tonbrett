package dev.schlaubi.tonbrett.app.web

import androidx.compose.ui.window.CanvasBasedWindow
import dev.schlaubi.tonbrett.app.TonbrettApp
import dev.schlaubi.tonbrett.app.title
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    onWasmReady {
        CanvasBasedWindow(title) {
            TonbrettApp()
        }
    }
}
