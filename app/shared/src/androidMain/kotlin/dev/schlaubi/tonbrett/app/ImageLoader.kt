@file:JvmName("ImageLoaderAndroid")

package dev.schlaubi.tonbrett.app

import com.seiko.imageloader.component.ComponentRegistryBuilder
import com.seiko.imageloader.component.decoder.BitmapFactoryDecoder
import com.seiko.imageloader.component.decoder.GifDecoder
import dev.schlaubi.tonbrett.app.api.AppContext
import kotlinx.coroutines.CoroutineScope

internal actual fun ComponentRegistryBuilder.registerComponents(
    appContext: AppContext,
    coroutineScope: CoroutineScope
) {
    add(GifDecoder.Factory())
    add(BitmapFactoryDecoder.Factory(appContext.androidContext, Int.MAX_VALUE))
}
