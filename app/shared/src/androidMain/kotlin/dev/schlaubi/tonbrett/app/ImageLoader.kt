@file:JvmName("ImageLoaderAndroid")

package dev.schlaubi.tonbrett.app

import android.os.Build
import coil3.ComponentRegistry
import coil3.decode.GifDecoder
import coil3.decode.ImageDecoderDecoder
import coil3.decode.SvgDecoder

internal actual fun ComponentRegistry.Builder.addPlatformComponents() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        add(ImageDecoderDecoder.Factory())
    } else {
        add(GifDecoder.Factory())
    }
    add(SvgDecoder.Factory())
}
