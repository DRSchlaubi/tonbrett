package dev.schlaubi.tonbrett.common.util

import kotlin.streams.asSequence

// Member is not MPP
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
internal actual fun String.codePoints(): Sequence<Int> =
    codePoints().asSequence()
