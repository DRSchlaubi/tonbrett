package dev.schlaubi.tonbrett.app.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.schlaubi.tonbrett.app.ErrorReporter
import dev.schlaubi.tonbrett.app.api.IO
import dev.schlaubi.tonbrett.app.api.LocalContext
import dev.schlaubi.tonbrett.app.strings.LocalStrings
import dev.schlaubi.tonbrett.common.Id
import dev.schlaubi.tonbrett.common.InterfaceAvailabilityChangeEvent
import dev.schlaubi.tonbrett.common.Sound
import dev.schlaubi.tonbrett.common.SoundCreatedEvent
import dev.schlaubi.tonbrett.common.SoundDeletedEvent
import dev.schlaubi.tonbrett.common.SoundUpdatedEvent
import dev.schlaubi.tonbrett.common.User
import dev.schlaubi.tonbrett.common.VoiceStateUpdateEvent
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val LOG = KotlinLogging.logger {}

@Composable
fun SoundList(errorReporter: ErrorReporter) {
    var playingSound by remember { mutableStateOf<Id<Sound>?>(null) }
    var sounds by remember { mutableStateOf<List<Sound>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var available by remember { mutableStateOf(true) }
    var channelMismatch by remember { mutableStateOf(false) }
    var offline by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val api = LocalContext.current.api

    fun reload() {
        loading = true
        coroutineScope.launch {
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
            channelMismatch = voiceState.channelMissMatch
        }
        if (didUpdate && !loading && !channelMismatch && !offline && sounds.isEmpty()) {
            reload()
        }
    }

    DisposableEffect(Unit) {
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
                        sounds = sounds.filter { it.id != event.sound.id } + event.sound
                    }

                    else -> LOG.warn { "Unknown event type: $event" }
                }
            }
            .launchIn(coroutineScope)

        onDispose { }
    }


    if (loading) {
        DisposableEffect(Unit) {
            coroutineScope.launch(Dispatchers.IO) {
                val state = try {
                    api.getMe().voiceState
                } catch (e: ClientRequestException) {
                    errorReporter(e)
                    return@launch
                }
                if (state == null) {
                    offline = true
                } else {
                    available = state.playerAvailable
                    playingSound = state.playingSound
                    offline = false
                    channelMismatch = state.channelMissMatch
                    if (!state.channelMissMatch) {
                        sounds = api.getSounds()
                    }
                }
                loading = false
            }
            onDispose { }
        }

        NonListBlock {
            CircularProgressIndicator()
        }
    }


    NonListBlock {
        val strings = LocalStrings.current
        when {
            offline -> ErrorText(strings.offline)
            channelMismatch -> ErrorText(strings.wrongChannelExplainer)
            !loading && sounds.isEmpty() -> ErrorText(strings.noSounds)
        }
    }

    if (!loading && !channelMismatch && !offline) {
        SoundContainer(sounds, errorReporter, playingSound, !available)
    }
}

@Composable
private fun NonListBlock(content: @Composable ColumnScope.() -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        content = content
    )
}
