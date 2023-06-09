package dev.schlaubi.tonbrett.app.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.schlaubi.tonbrett.app.ErrorReporter
import dev.schlaubi.tonbrett.app.api.IO
import dev.schlaubi.tonbrett.app.api.LocalContext
import dev.schlaubi.tonbrett.app.strings.LocalStrings
import dev.schlaubi.tonbrett.app.util.canClearFocus
import dev.schlaubi.tonbrett.common.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging

private val LOG = KotlinLogging.logger {}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SoundList(errorReporter: ErrorReporter, voiceState: User.VoiceState?) {
    var playingSound by remember { mutableStateOf<Id<Sound>?>(null) }
    var sounds by remember { mutableStateOf<List<Sound>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var available by remember { mutableStateOf(voiceState?.playerAvailable ?: false) }
    var channelMismatch by remember { mutableStateOf(voiceState?.channelMismatch ?: false) }
    var offline by remember { mutableStateOf(voiceState == null) }
    val coroutineScope = rememberCoroutineScope()
    val api = LocalContext.current.api

    fun reload() {
        loading = true
        coroutineScope.launch(Dispatchers.IO) {
            sounds = api.getSounds()
            loading = false
        }
    }

    fun reload(voiceState: User.VoiceState?) {
        val didUpdate =
            offline != (voiceState == null) ||
                    available != voiceState?.playerAvailable ||
                    playingSound != voiceState.playingSound

        if (voiceState == null) {
            offline = true
        } else {
            available = voiceState.playerAvailable
            playingSound = voiceState.playingSound
            offline = false
            channelMismatch = voiceState.channelMismatch
        }
        if (didUpdate && !loading && !channelMismatch && !offline && sounds.isEmpty()) {
            reload()
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            api.events
                .onEach { event ->
                    when (event) {
                        is VoiceStateUpdateEvent -> reload(event.voiceState)
                        is InterfaceAvailabilityChangeEvent -> {
                            playingSound = event.playingSongId
                            available = event.available
                        }

                        is SoundDeletedEvent -> {
                            sounds = sounds.filter { it.id != event.id }
                        }

                        is SoundCreatedEvent -> {
                            sounds += event.sound
                        }

                        is SoundUpdatedEvent -> {
                            val copy = sounds.toMutableList()
                            copy[copy.indexOfFirst { it.id == event.sound.id }] = event.sound
                            sounds = copy
                        }

                        else -> LOG.warn { "Unknown event type: $event" }
                    }
                }
                .launchIn(this)
        }
    }


    if (loading) {
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                if (voiceState?.channelMismatch == false) {
                    sounds = api.getSounds()
                }
                loading = false
            }
        }

        NonListBlock {
            CircularProgressIndicator()
        }
    }

    Column {
        val renderingSounds = !loading && !channelMismatch && !offline
        AnimatedContent(renderingSounds) { render ->
            if (render) {
                SoundContainer(sounds, errorReporter, playingSound, !available) {
                    sounds = it
                }
            }
        }

        NonListBlock(Modifier.canClearFocus()) {
            val strings = LocalStrings.current
            when {
                offline -> ErrorText(strings.offline)
                channelMismatch -> ErrorText(strings.wrongChannelExplainer)
                !loading && sounds.isEmpty() -> ErrorText(strings.noSounds)
            }
        }
    }
}

@Composable
private fun NonListBlock(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth().padding(vertical = 10.dp),
        content = content
    )
}
