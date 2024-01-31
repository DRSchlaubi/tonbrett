@file:JvmName("ImageLoaderSkia")

package dev.schlaubi.tonbrett.app

import coil3.ComponentRegistry
import coil3.decode.SkiaImageDecoder
import kotlin.jvm.JvmName

internal actual fun ComponentRegistry.Builder.addPlatformComponents() {
    add(SkiaImageDecoder.Factory())
}
