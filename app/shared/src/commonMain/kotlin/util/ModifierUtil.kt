package dev.schlaubi.tonbrett.app.util

import androidx.compose.ui.Modifier

/**
 * Applies [modifier] if [condition] is true.
 */
fun Modifier.conditional(
    condition: Boolean,
    modifier: (Modifier).() -> Modifier
): Modifier {
    return if (condition) {
        modifier()
    } else {
        this
    }
}
