@file:JvmName("ImageLoaderAndroid")

package dev.schlaubi.tonbrett.app

import com.seiko.imageloader.component.ComponentRegistryBuilder
import com.seiko.imageloader.component.decoder.BitmapFactoryDecoder
import dev.schlaubi.tonbrett.app.api.AppContext

internal actual fun ComponentRegistryBuilder.registerComponents(appContext: AppContext) {
    add(BitmapFactoryDecoder.Factory(appContext.androidContext, Int.MAX_VALUE))
}
