package dev.schlaubi.tonbrett.app.util

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import dev.schlaubi.tonbrett.app.ColorScheme
import kotlin.math.roundToInt

@Composable
fun SoundToolTip(text: String?, isShown: Boolean, content: @Composable () -> Unit) {
    if (text == null) return content()

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(Modifier.hoverable(interactionSource)) {
        content()

        if (isShown || isHovered) {
            val density = LocalDensity.current
            var height by remember { mutableStateOf(0) }

            fun whenPositionedOrTransparent(color: Color) = if (height > 0) color else Color.Transparent

            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, with(density) { (-(5).dp.roundToPx() - height) })
            ) {
                SelectionContainer {
                    Text(
                        text,
                        color = whenPositionedOrTransparent(ColorScheme.textColor),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(whenPositionedOrTransparent(Color.Black), RoundedCornerShape(7.dp))
                            .onSizeChanged {
                                height = it.height
                            }
                            .hoverable(interactionSource)
                            .padding(horizontal = 5.dp, vertical = 10.dp)
                            .widthIn(min = 160.dp, max = 250.dp)
                    )
                }
            }
        }
    }
}
