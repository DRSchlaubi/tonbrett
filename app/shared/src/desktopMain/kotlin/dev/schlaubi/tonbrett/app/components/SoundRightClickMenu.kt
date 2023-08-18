package dev.schlaubi.tonbrett.app.components

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.runtime.Composable
import cafe.adriel.lyricist.LocalStrings
import dev.schlaubi.tonbrett.app.api.LocalContext
import dev.schlaubi.tonbrett.common.Id
import dev.schlaubi.tonbrett.common.Route
import dev.schlaubi.tonbrett.common.Sound
import io.ktor.http.*
import org.jetbrains.skiko.ClipboardManager

@Composable
private fun CopySoundUrl(id: Id<Sound>): ContextMenuItem {
    val api = LocalContext.current.api
    return ContextMenuItem(LocalStrings.current.copyUrl) {
        ClipboardManager().setText(api.href(Route.Sounds.Sound.Audio(id.toString(), ContentType.Audio.MPEG)))
    }
}

@Composable
actual fun SoundCardContextMenuArea(sound: Id<Sound>, content: @Composable () -> Unit) {
    val element = CopySoundUrl(sound)
    ContextMenuArea({ listOf(element) }, content = content)
}
