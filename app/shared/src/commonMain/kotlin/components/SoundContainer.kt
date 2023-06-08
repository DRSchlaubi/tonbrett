package dev.schlaubi.tonbrett.app.components

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.schlaubi.tonbrett.app.ColorScheme
import dev.schlaubi.tonbrett.app.ErrorReporter
import dev.schlaubi.tonbrett.app.OptionalWebImage
import dev.schlaubi.tonbrett.app.api.IO
import dev.schlaubi.tonbrett.app.api.LocalContext
import dev.schlaubi.tonbrett.app.util.canClearFocus
import dev.schlaubi.tonbrett.app.util.conditional
import dev.schlaubi.tonbrett.app.util.isMobile
import dev.schlaubi.tonbrett.common.Id
import dev.schlaubi.tonbrett.common.Sound
import io.ktor.client.plugins.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SoundContainer(
    sounds: List<Sound>,
    errorReporter: ErrorReporter,
    playingSound: Id<Sound>?,
    disabled: Boolean,
    soundUpdater: SoundUpdater
) {
    Column {
        SearchBarScope(soundUpdater) {
            LazyVerticalGrid(GridCells.Adaptive(160.dp), Modifier.canClearFocus().fillMaxHeight()) {
                items(sounds) { (id, name, _, description, emoji) ->
                    SoundCard(id, name, emoji, description, id == playingSound, errorReporter, disabled)
                }
            }

            if (disabled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ColorScheme.disabled.copy(alpha = .4f))
                        .zIndex(1f)
                ) {}
            }
        }
    }
}

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

    fun request(block: suspend CoroutineScope.() -> Unit) = coroutineScope.launch(Dispatchers.IO) {
        try {
            block()
        } catch (e: ClientRequestException) {
            reportError(e)
        }
    }

    fun play() = request {
        api.play(id.toString())
    }

    fun stop() = request { api.stop() }

    ElevatedCard(
        colors = CardDefaults.cardColors(containerColor = ColorScheme.secondaryContainer),
        shape = corners,
        modifier = Modifier.size(width = 128.dp, height = 64.dp)
            .padding(vertical = 3.dp, horizontal = 5.dp)
            .conditional(playing) {
                border(BorderStroke(2.dp, ColorScheme.active), corners)
            }
            .hoverable(interactionSource)
            .conditional(isMobile) {
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
                    if (!disabled) {
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
                val url = (emoji as? Sound.Emoji.HasUrl)?.url ?: error("This emoji is invalid: $emoji")
                OptionalWebImage(url, modifier = Modifier.size(32.dp).padding(end = 5.dp))
                Text(name, color = ColorScheme.textColor, fontSize = 16.sp, textAlign = TextAlign.Center)
            }
            if (playing && hovered && !disabled) {
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
    if (description != null && hovered && !playing) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .shadow(2.dp)
                .zIndex(2f)
                .background(ColorScheme.secondaryContainer)
                .padding(8.dp),
        ) {
            Text(description, color = ColorScheme.textColor)
        }
    }
}
