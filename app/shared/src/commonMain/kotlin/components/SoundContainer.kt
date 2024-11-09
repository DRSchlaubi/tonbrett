@file:Suppress("INVISIBLE_MEMBER")

package dev.schlaubi.tonbrett.app.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.TransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.schlaubi.tonbrett.app.ColorScheme
import dev.schlaubi.tonbrett.app.ErrorReporter
import dev.schlaubi.tonbrett.app.LocalStrings
import dev.schlaubi.tonbrett.app.OptionalWebImage
import dev.schlaubi.tonbrett.app.api.IO
import dev.schlaubi.tonbrett.app.api.LocalContext
import dev.schlaubi.tonbrett.app.util.FlowGrid
import dev.schlaubi.tonbrett.app.util.SoundToolTip
import dev.schlaubi.tonbrett.app.util.canClearFocus
import dev.schlaubi.tonbrett.app.util.conditional
import dev.schlaubi.tonbrett.app.util.isMobile
import dev.schlaubi.tonbrett.common.Id
import dev.schlaubi.tonbrett.common.Sound
import dev.schlaubi.tonbrett.common.SoundGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.ceil

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SoundContainer(model: SoundListViewModel) {
    val state by model.uiState.collectAsState()
    val strings = LocalStrings.current

    Column {
        val unavailableFor = when {
            state.offline -> strings.offline
            state.channelMismatch -> strings.wrongChannelExplainer
            state.sounds.isEmpty() -> strings.noSounds
            !state.available -> strings.playerBusy
            else -> null
        }
        AnimatedVisibility(
            unavailableFor != null,
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
            label = unavailableFor.toString()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp)
                    .background(ColorScheme.current.error)
            ) {
                Text(
                    unavailableFor!!,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 25.sp
                )
            }
        }

        SearchBar(model::updateSounds)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.fillMaxSize()
                .padding(vertical = 5.dp)
                .scrollable(rememberScrollState(), Orientation.Vertical)
        ) {
            items(state.sounds) { group ->
                SoundGroup(group, state.playingSound, model::reportError, unavailableFor)
            }
        }
    }
}

@Composable
private fun SoundGroup(
    group: SoundGroup,
    playingSound: Id<Sound>?,
    errorReporter: ErrorReporter,
    unavailableFor: String?
) = BoxWithConstraints(Modifier.padding(horizontal = 5.dp)) {
    val maxWidth = constraints.maxWidth
    var visible by remember { mutableStateOf(true) }

    val strings = LocalStrings.current

    Column {
        // Header section with title and collapse icon
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(group.tag ?: strings.otherSounds, color = ColorScheme.current.textColor)
            CollapseIcon(
                visible,
                Modifier.clickable { visible = !visible }
            )
            HorizontalDivider(Modifier.fillMaxWidth().padding(5.dp))
        }

        val density = LocalDensity.current
        val cells = remember { GridCells.Adaptive(160.dp) }
        val spacing = remember(density) { with(density) { 3.dp.roundToPx() } }

        // Rows calculation memoized based on parameters
        val rows = remember(spacing, maxWidth, group.sounds.size) {
            val itemsPerRow = with(cells) {
                density.calculateCrossAxisCellSizes(maxWidth, spacing).size.toDouble()
            }
            ceil(group.sounds.size / itemsPerRow).toInt()
        }

        // Efficiently control visibility animation
        AnimatedVisibility(visible) {
            Box(Modifier.height((64.dp + 3.dp) * rows)) {
                LazyVerticalGrid(
                    columns = cells,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    userScrollEnabled = false,  // If you want no scrolling
                    modifier = Modifier.canClearFocus().fillMaxWidth()
                ) {
                    items(group.sounds, { it.id.toString() }) { sound ->
                        SoundCard(
                            id = sound.id,
                            name = sound.name,
                            emoji = sound.emoji,
                            description = sound.description,
                            playing = sound.id == playingSound,
                            reportError = errorReporter,
                            disabled = unavailableFor != null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollapseIcon(visible: Boolean, modifier: Modifier = Modifier) {
    val transition = updateTransition(visible)
    val iconRotation by transition.animateFloat { visible ->
        if (!visible) -90f else 0f
    }
    Icon(
        Icons.Default.ExpandMore,
        null,
        tint = ColorScheme.current.textColor,
        modifier = modifier.rotate(iconRotation)
    )
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
    val coroutineScope = rememberCoroutineScope { Dispatchers.IO }
    val corners = RoundedCornerShape(10.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val api = LocalContext.current.api
    var waiting by remember { mutableStateOf(false) }

    fun request(block: suspend CoroutineScope.() -> Unit) = coroutineScope.launch {
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
                colors = CardDefaults.cardColors(containerColor = ColorScheme.current.secondaryContainer),
                shape = corners,
                modifier = Modifier.height(height = 64.dp)
                    .fillMaxWidth()
                    .conditional(playing) {
                        border(BorderStroke(2.dp, ColorScheme.current.active), corners)
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
                        OptionalWebImage(emoji?.url?.proxyUrl(), modifier = Modifier.size(32.dp).padding(end = 5.dp))
                        Text(
                            name,
                            color = ColorScheme.current.textColor,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    if (playing && hovered && !disabled && !waiting) {
                        Box(Modifier.zIndex(1f).background(ColorScheme.current.secondaryContainer)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                IconButton({ play() }) {
                                    Icon(Icons.Default.Refresh, null, tint = ColorScheme.current.textColor)
                                }
                                VerticalDivider(Modifier.width(1.dp).height(15.dp))
                                IconButton({
                                    stop()
                                }) {
                                    Icon(Icons.Default.Stop, null, tint = ColorScheme.current.textColor)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

expect fun String.proxyUrl(): String
