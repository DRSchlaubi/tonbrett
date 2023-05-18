package dev.schlaubi.tonbrett.common.util

internal fun formatEmojiUnicode(input: String) = input.map { it.code }
    .joinToString("-") { it.toString(16) }
