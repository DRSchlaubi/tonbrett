package dev.schlaubi.tonbrett.app.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/**
 * Utility function reversing the layout of a [Row].
 *
 * This can be usefuly if you want to layout something from Right to Left, but still render it from left to right
 */
@Composable
inline fun ReverseRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    crossinline content: @Composable RowScope.() -> Unit
) {
    ProvideLayoutDirection(LayoutDirection.Rtl) {
        Row(modifier, horizontalArrangement, verticalAlignment) {
            ProvideLayoutDirection(LayoutDirection.Ltr) {
                content()
            }
        }
    }
}

/**
 * Utility function that changes the [LocalLayoutDirection] to [direction].
 */
@Composable
inline fun ProvideLayoutDirection(direction: LayoutDirection, noinline content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides direction, content = content)
}
