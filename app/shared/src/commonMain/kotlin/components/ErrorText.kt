package dev.schlaubi.tonbrett.app.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import dev.schlaubi.tonbrett.app.ColorScheme

@Composable
fun ErrorText(message: String) {
    Text(message, color = ColorScheme.textColor, fontSize = 3.em, textAlign = TextAlign.Center)
}
