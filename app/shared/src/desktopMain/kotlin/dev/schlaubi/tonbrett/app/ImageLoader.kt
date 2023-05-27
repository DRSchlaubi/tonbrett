@file:JvmName("ImageLoaderDesktop")

package dev.schlaubi.tonbrett.app

import com.seiko.imageloader.component.ComponentRegistryBuilder
import com.seiko.imageloader.component.decoder.SkiaImageDecoder
import dev.schlaubi.tonbrett.app.api.AppContext

internal actual fun ComponentRegistryBuilder.registerComponents(appContext: AppContext) {
    add(SkiaImageDecoder.Factory())
}
