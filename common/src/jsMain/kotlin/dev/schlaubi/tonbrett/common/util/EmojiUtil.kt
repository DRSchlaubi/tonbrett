package dev.schlaubi.tonbrett.common.util

internal actual fun String.codePoints(): Sequence<Int> = buildList(length) {
    var i = 0
    while (isNotEmpty()) {
        val point = asDynamic().codePointAt(i++) as Int?
        if (point != null) {
            add(point)
        } else {
            break
        }
    }
}.asSequence()
