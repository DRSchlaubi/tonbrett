package dev.schlaubi.tonbrett.app.components

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import dev.schlaubi.tonbrett.app.LocalStrings
import dev.schlaubi.tonbrett.app.api.LocalContext
import dev.schlaubi.tonbrett.common.Id
import dev.schlaubi.tonbrett.common.Route
import dev.schlaubi.tonbrett.common.Sound
import io.ktor.http.*
import kotlinx.coroutines.launch
import java.awt.datatransfer.StringSelection

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun CopySoundUrl(id: Id<Sound>): ContextMenuItem {
    val api = LocalContext.current.api
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    return ContextMenuItem(LocalStrings.current.copyUrl) {
        scope.launch {
            val url = api.href(Route.Sounds.Sound.Audio(id.toString(), ContentType.Audio.MPEG))
            val entry = ClipEntry(StringSelection(url))
            clipboard.setClipEntry(entry)
        }
    }
}

@Composable
actual fun SoundCardContextMenuArea(sound: Id<Sound>, content: @Composable () -> Unit) {
    val element = CopySoundUrl(sound)
    ContextMenuArea({ listOf(element) }, content = content)
}
