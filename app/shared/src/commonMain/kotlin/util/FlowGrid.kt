package dev.schlaubi.tonbrett.app.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Grid-like layout based on [FlowRow].
 *
 * @param gridCells descriptor of [GridCells]
 * @param items the items inside the grid
 * @param horizontalArrangement The horizontal arrangement of the layout's children.
 * @param verticalArrangement The vertical arrangement of the layout's virtual rows.
 * @param modifier the [Modifier]
 * @param itemContent Content for the items
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> FlowGrid(
    gridCells: GridCells,
    items: Iterable<T>,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit
) = BoxWithConstraints(modifier) {
    val density = LocalDensity.current
    val spacingPx = remember(density) { with(density) { horizontalArrangement.spacing.roundToPx() } }
    val widths =
        remember(density, constraints) { with(gridCells) { density.calculateCrossAxisCellSizes(constraints.maxWidth, spacingPx) } }

    fun getWidth(index: Int) = with(density) { widths[index % widths.size].toDp() }

    FlowRow(
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
    ) {
        items.forEachIndexed { index, item ->
            val width = getWidth(index)

            BoxWithConstraints(Modifier.width(width)) {
                itemContent(item)
            }
        }
    }
}
