package dev.schlaubi.tonbrett.app.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

object SyntaxHighlightingTextTransformation : VisualTransformation {
    private val operators = listOf("name", "tag", "description")
    override fun filter(text: AnnotatedString): TransformedText {
        val split = text.split(":")
        val operator = split.first()
        val (string, offset) = if (operator in operators) {
            buildAnnotatedString {
                val remaining = split.drop(1).joinToString(":")
                append(AnnotatedString("$operator:", SpanStyle(background = Color.DarkGray)))
                append(' ') // let's place some space after operators
                append(remaining)
            } to SyntaxOffsetMapping(operator)
        } else {
            text to OffsetMapping.Identity
        }

        return TransformedText(
            string,
            offset
        )
    }
}

private class SyntaxOffsetMapping(operator: String) : OffsetMapping {
    val switchPoint = operator.length - 1
    override fun originalToTransformed(offset: Int): Int {
        return if (offset <= switchPoint) {
            offset
        } else {
            offset + 1
        }
    }

    override fun transformedToOriginal(offset: Int): Int {
        return if (offset <= switchPoint) {
            offset
        } else {
            offset - 1
        }
    }
}
