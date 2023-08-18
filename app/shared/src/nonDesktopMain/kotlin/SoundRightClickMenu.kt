package dev.schlaubi.tonbrett.app.components

import androidx.compose.runtime.Composable
import dev.schlaubi.tonbrett.common.Id
import dev.schlaubi.tonbrett.common.Sound

// On platforms not supporting context menus, we just don't add anything and call the content block
@Composable
actual fun SoundCardContextMenuArea(sound: Id<Sound>, content: @Composable () -> Unit) = content()
