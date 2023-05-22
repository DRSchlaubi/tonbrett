@file:JvmName("SkiaImageLoader")

package dev.schlaubi.tonbrett.app

import com.seiko.imageloader.component.ComponentRegistryBuilder
import com.seiko.imageloader.component.decoder.SkiaImageDecoder
import dev.schlaubi.tonbrett.app.api.AppContext
import kotlin.jvm.JvmName

internal actual fun ComponentRegistryBuilder.registerComponents(appContext: AppContext) {
    add(SkiaImageDecoder.Factory())
}
