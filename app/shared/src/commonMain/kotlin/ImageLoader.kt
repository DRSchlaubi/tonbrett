package dev.schlaubi.tonbrett.app

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import coil3.ComponentRegistry
import coil3.ImageLoader
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.ktor.KtorNetworkFetcherFactory
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.schlaubi.tonbrett.app.api.AppContext
import mu.KotlinLogging
import okio.Path

private val LOG = KotlinLogging.logger { }

internal expect fun ComponentRegistry.Builder.addPlatformComponents()

@Composable
internal expect fun isWindowMinimized(): Boolean

@Composable
fun OptionalWebImage(url: String?, contentDescription: String? = null, modifier: Modifier = Modifier) =
    OptionalWebImageInternal(url, contentDescription, modifier)

private val LocalImageLoader = staticCompositionLocalOf<ImageLoader> {
    error("no imageloader set")
}

@Composable
fun ProvideImageLoader(imageLoader: ImageLoader, content: @Composable () -> Unit) = CompositionLocalProvider(
    LocalImageLoader provides imageLoader,
    content = content
)

@Composable
private fun OptionalWebImageInternal(url: String?, contentDescription: String?, modifier: Modifier) {
    if (url != null) {
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalPlatformContext.current).data(url).build(),
            imageLoader = LocalImageLoader.current
        )
        val state = painter.state
        if (state is AsyncImagePainter.State.Error) {
            LOG.warn(state.result.throwable) { "Could not load image $url" }
        } else if (!isWindowMinimized()) {
            Image(painter, contentDescription, modifier)
        }
    }
}

fun newImageLoader(appContext: AppContext): ImageLoader = ImageLoader.Builder(appContext.platformContext).components {
    add(KtorNetworkFetcherFactory())
    addPlatformComponents()
}.memoryCache {
    MemoryCache.Builder()
        // Set the max size to 25% of the app's available memory.
        .maxSizePercent(appContext.platformContext, percent = 0.25).build()
}.diskCache { newDiskCache() }.crossfade(false).build()

expect fun newDiskCache(): DiskCache?

internal fun fileSystemDiskCache(directory: Path) =
    DiskCache.Builder().directory(directory / "image_cache")
        .maxSizeBytes(512L * 1024 * 1024) // 512MB
        .build()
