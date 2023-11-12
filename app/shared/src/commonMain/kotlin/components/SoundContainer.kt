@file:Suppress("INVISIBLE_MEMBER")

package dev.schlaubi.tonbrett.app.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.schlaubi.tonbrett.app.ColorScheme
import dev.schlaubi.tonbrett.app.ErrorReporter
import dev.schlaubi.tonbrett.app.OptionalWebImage
import dev.schlaubi.tonbrett.app.api.IO
import dev.schlaubi.tonbrett.app.api.LocalContext
import dev.schlaubi.tonbrett.app.util.SoundToolTip
import dev.schlaubi.tonbrett.app.util.canClearFocus
import dev.schlaubi.tonbrett.app.util.conditional
import dev.schlaubi.tonbrett.app.util.isMobile
import dev.schlaubi.tonbrett.common.Id
import dev.schlaubi.tonbrett.common.Sound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SoundContainer(
    sounds: List<Sound>,
    errorReporter: ErrorReporter,
    playingSound: Id<Sound>?,
    unavailableFor: String?,
    soundUpdater: SoundUpdater
) {
    var lastDisabledReason by remember { mutableStateOf(unavailableFor) }
    if (unavailableFor != null) {
        lastDisabledReason = unavailableFor
    }

    Column {
        AnimatedVisibility(
            unavailableFor != null,
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
            label = unavailableFor.toString()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp)
                    .background(ColorScheme.error)
            ) {
                Text(
                    lastDisabledReason!!,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 25.sp
                )
            }
        }

        SearchBarScope(soundUpdater) {
            LazyVerticalGrid(
                GridCells.Adaptive(160.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.canClearFocus().fillMaxHeight().padding(5.dp)
            ) {
                items(sounds) { (id, name, _, description, emoji) ->
                    SoundCard(id, name, emoji, description, id == playingSound, errorReporter, unavailableFor != null)
                }
            }
        }

    }
}

@Composable
expect fun SoundCardContextMenuArea(sound: Id<Sound>, content: @Composable () -> Unit)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SoundCard(
    id: Id<Sound>,
    name: String,
    emoji: Sound.Emoji?,
    description: String?,
    playing: Boolean,
    reportError: ErrorReporter,
    disabled: Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    val corners = RoundedCornerShape(10.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val api = LocalContext.current.api
    var waiting by remember { mutableStateOf(false) }

    fun request(block: suspend CoroutineScope.() -> Unit) = coroutineScope.launch(Dispatchers.IO) {
        waiting = true
        try {
            block()
        } catch (e: Exception) {
            reportError(e)
        } finally {
            waiting = false
        }
    }

    fun play() = request {
        api.play(id.toString())
    }

    fun stop() = request { api.stop() }

    SoundToolTip(description, hovered && !playing) {
        SoundCardContextMenuArea(id) {
            ElevatedCard(
                colors = CardDefaults.cardColors(containerColor = ColorScheme.secondaryContainer),
                shape = corners,
                modifier = Modifier.height(height = 64.dp)
                    .fillMaxWidth()
                    .conditional(playing) {
                        border(BorderStroke(2.dp, ColorScheme.active), corners)
                    }
                    .hoverable(interactionSource)
                    .conditional(isMobile && !waiting && !disabled) {
                        combinedClickable(
                            onClick = {
                                if (playing) {
                                    stop()
                                } else {
                                    play()
                                }
                            },
                            onDoubleClick = {
                                if (playing && !disabled) {
                                    play()
                                }
                            }
                        )
                    }.conditional(!isMobile) {
                        clickable {
                            if (!disabled && !waiting) {
                                play()
                            }
                        }
                    }
            ) {
                BoxWithConstraints {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 3.dp)
                    ) {
                        require(emoji is Sound.Emoji.HasUrl?) { "This emoji is invalid: $emoji" }
                        OptionalWebImage(emoji?.url, modifier = Modifier.size(32.dp).padding(end = 5.dp))
                        Text(name, color = ColorScheme.textColor, fontSize = 16.sp, textAlign = TextAlign.Center)
                    }
                    if (playing && hovered && !disabled && !waiting) {
                        Box(Modifier.zIndex(1f).background(ColorScheme.secondaryContainer)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                IconButton({
                                    play()
                                }) {
                                    Icon(Icons.Default.Refresh, null, tint = ColorScheme.textColor)
                                }
                                Divider(Modifier.width(1.dp).height(15.dp))
                                IconButton({
                                    stop()
                                }) {
                                    Icon(Icons.Default.Stop, null, tint = ColorScheme.textColor)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
