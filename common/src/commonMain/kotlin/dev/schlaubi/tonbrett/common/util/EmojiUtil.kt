@file:JvmName("EmojiUtilJvm")

package dev.schlaubi.tonbrett.common.util

import kotlin.jvm.JvmName
import dev.schlaubi.tonbrett.common.util.codePoints as commonCodePoints

public expect fun String.codePoints(): Sequence<Int>
internal fun formatEmojiUnicode(input: String) = input.commonCodePoints()
    // twemoji seems to use variant selectors weirdly
    .filterIndexed { index, it ->
        (index != 1 && input.length != 2) || it !in 0xFE0B..0xFE0F
    }
    .joinToString("-") { it.toString(16) }
