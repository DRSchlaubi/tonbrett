@file:JvmName("ImageLoaderDesktop")

package dev.schlaubi.tonbrett.app

import com.seiko.imageloader.component.ComponentRegistryBuilder
import com.seiko.imageloader.component.decoder.GifDecoder
import com.seiko.imageloader.component.decoder.SkiaImageDecoder
import dev.schlaubi.tonbrett.app.api.AppContext
import kotlinx.coroutines.CoroutineScope

internal actual fun ComponentRegistryBuilder.registerComponents(appContext: AppContext, coroutineScope: CoroutineScope) {
    add(GifDecoder.Factory(coroutineScope))
    add(SkiaImageDecoder.Factory())
}
