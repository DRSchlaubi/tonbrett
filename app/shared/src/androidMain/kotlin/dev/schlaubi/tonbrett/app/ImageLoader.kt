@file:JvmName("ImageLoaderAndroid")

package dev.schlaubi.tonbrett.app

import android.os.Build
import coil3.ComponentRegistry
import coil3.decode.BitmapFactoryDecoder
import coil3.gif.GifDecoder
import coil3.svg.SvgDecoder

internal actual fun ComponentRegistry.Builder.addPlatformComponents() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        add(BitmapFactoryDecoder.Factory())
    } else {
        add(GifDecoder.Factory())
    }
    add(SvgDecoder.Factory())
}
