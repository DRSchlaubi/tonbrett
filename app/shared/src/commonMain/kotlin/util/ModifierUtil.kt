package dev.schlaubi.tonbrett.app.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Applies [modifier] if [condition] is true.
 */
@Composable
fun Modifier.conditional(
    condition: Boolean,
    modifier: @Composable ((Modifier).() -> Modifier)
): Modifier {
    return if (condition) {
        modifier()
    } else {
        this
    }
}
