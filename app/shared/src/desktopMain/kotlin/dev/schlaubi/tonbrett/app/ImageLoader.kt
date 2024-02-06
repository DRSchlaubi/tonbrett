@file:JvmName("ImageLoaderDesktop")

package dev.schlaubi.tonbrett.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.awt.ComposeWindow
import coil3.disk.DiskCache
import okio.Path
import okio.Path.Companion.toPath

const val uwpTempFolder = "dev.schlaubi.tonbrett.app.desktop.windows.uwp_temp_folder"

private val LocalWindow = staticCompositionLocalOf<ComposeWindow> { error("No window") }

@Composable
fun ProvideLocalWindow(windowState: ComposeWindow, content: @Composable () -> Unit) = CompositionLocalProvider(
    LocalWindow provides windowState,
    content = content
)

@Composable
internal actual fun isWindowMinimized(): Boolean = LocalWindow.current.isMinimized

private fun getBasePath(): Path {
    val os = System.getProperty("os.name")
    val basePath = when {
        os.contains("windows", ignoreCase = true) -> {
            val uwpTemp = System.getProperty(uwpTempFolder)
            if (uwpTemp.isNullOrBlank()) {
                System.getenv("APPDATA").toPath()
            } else {
                return uwpTemp.toPath()
            }
        }

        os.contains("mac", ignoreCase = true) ->
            System.getenv("HOME").toPath() / "Library" / "Application Support"

        else -> System.getProperty("user.home").toPath()
    }

    return basePath / "Tonbrett"
}

actual fun newDiskCache(): DiskCache? = fileSystemDiskCache(getBasePath())
