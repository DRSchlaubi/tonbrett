@file:JvmName("ImageLoaderJvm")

package dev.schlaubi.tonbrett.app

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.ImageRequestState
import com.seiko.imageloader.LocalImageLoader
import com.seiko.imageloader.component.ComponentRegistryBuilder
import com.seiko.imageloader.component.setupKtorComponents
import com.seiko.imageloader.rememberAsyncImagePainter
import dev.schlaubi.tonbrett.app.api.AppContext
import dev.schlaubi.tonbrett.app.api.LocalContext
import kotlinx.coroutines.CoroutineScope
import mu.KotlinLogging

private val LOG = KotlinLogging.logger { }

internal expect fun ComponentRegistryBuilder.registerComponents(appContext: AppContext, coroutineScope: CoroutineScope)

@Composable
actual fun OptionalWebImageInternal(url: String?, contentDescription: String?, modifier: Modifier) {
    if (url != null) {
        val painter = rememberAsyncImagePainter(url)
        val state = painter.requestState
        if (state is ImageRequestState.Failure) {
            LOG.warn(state.error) { "Could not load image $url" }
        } else {
            Image(painter, contentDescription, modifier)
        }
    }
}

@Composable
fun ProvideImageLoader(content: @Composable () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    CompositionLocalProvider(
        LocalImageLoader provides newImageLoader(LocalContext.current, coroutineScope),
        content = content
    )
}

private fun newImageLoader(appContext: AppContext, coroutineScope: CoroutineScope): ImageLoader = ImageLoader {
    components {
        setupKtorComponents()
        registerComponents(appContext, coroutineScope)
    }
}
