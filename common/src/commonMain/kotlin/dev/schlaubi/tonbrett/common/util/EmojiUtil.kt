@file:JvmName("EmojiUtilJvm")

package dev.schlaubi.tonbrett.common.util

import kotlin.jvm.JvmName

internal fun formatEmojiUnicode(codePoints: Sequence<Int>, length: Int) = codePoints
    // twemoji seems to use variant selectors weirdly
    .filterIndexed { index, it ->
        (index != 1 && length != 2) || it !in 0xFE0B..0xFE0F
    }
    .joinToString("-") { it.toString(16) }
