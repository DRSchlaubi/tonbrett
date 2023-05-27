package dev.schlaubi.tonbrett.app.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun Modifier.canClearFocus(): Modifier {
    val interactionSource = remember { MutableInteractionSource() }

    // If you can click a composable it "steals" the focus
    return clickable(interactionSource, indication = null) {}
}
