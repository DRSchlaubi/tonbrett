package dev.schlaubi.tonbrett.app.components

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.schlaubi.tonbrett.app.ColorScheme
import dev.schlaubi.tonbrett.app.ErrorReporter
import dev.schlaubi.tonbrett.app.OptionalWebImage
import dev.schlaubi.tonbrett.app.api.LocalContext
import dev.schlaubi.tonbrett.app.util.conditional
import dev.schlaubi.tonbrett.common.Id
import dev.schlaubi.tonbrett.common.Sound
import io.ktor.client.plugins.*
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
            LazyVerticalGrid(GridCells.Adaptive(160.dp)) {
                items(sounds) { (id, name, _, description, emoji) ->
                    SoundCard(id, name, emoji, description, id == playingSound, errorReporter, disabled)
                }
            }

            if (disabled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ColorScheme.disabled.copy(alpha = .4f))
                        .zIndex(100f)
                ) {}
            }
        }
    }
}

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

    ElevatedCard(
        colors = CardDefaults.cardColors(containerColor = ColorScheme.secondaryContainer),
        shape = corners,
        modifier = Modifier.size(width = 128.dp, height = 64.dp)
            .padding(vertical = 3.dp, horizontal = 5.dp)
            .conditional(playing) {
                border(BorderStroke(2.dp, ColorScheme.active), corners)
            }
            .hoverable(interactionSource)
            .run {
                if (!disabled) {
                    clickable {
                        coroutineScope.launch {
                            try {
                                api.play(id.toString())
                            } catch (e: ClientRequestException) {
                                reportError(e)
                            }
                        }
                    }
                } else {
                    this
                }
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(horizontal = 3.dp)
        ) {
            OptionalWebImage(emoji?.url, modifier = Modifier.size(32.dp).padding(end = 5.dp))
            Text(name, color = ColorScheme.textColor, fontSize = 16.sp)
        }
    }
    if (description != null && hovered && !playing) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .shadow(2.dp)
                .background(ColorScheme.secondaryContainer)
                .padding(8.dp),
        ) {
            Text(description, color = ColorScheme.textColor)
        }
    }
}
