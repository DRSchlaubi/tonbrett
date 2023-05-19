package dev.schlaubi.tonbrett.app.components

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
import dev.schlaubi.tonbrett.app.api.api
import dev.schlaubi.tonbrett.common.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Composable
fun SoundList(errorReporter: ErrorReporter) {
    var playingSound by remember { mutableStateOf<Id<Sound>?>(null) }
    var sounds by remember { mutableStateOf<List<Sound>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var available by remember { mutableStateOf(true) }
    var botOffline by remember { mutableStateOf(false) }
    var channelMismatch by remember { mutableStateOf(false) }
    var offline by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun reload() {
        loading = true
        coroutineScope.launch {
            sounds = api.getSounds()
            loading = false
        }
    }

    fun reload(voiceState: User.VoiceState?) {
        if (voiceState == null) {
            offline = true
        } else {
            available = voiceState.playerAvailable
            playingSound = voiceState.playingSound
            offline = false
            botOffline = voiceState.botOffline
            channelMismatch = voiceState.channelMissMatch
        }
        if (!loading && !botOffline && !channelMismatch && !offline) {
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

                    else -> println("Unknown event type: $event")
                }
            }
            .launchIn(coroutineScope)

        onDispose { }
    }


    if (loading) {
        DisposableEffect(Unit) {
            coroutineScope.launch {
                val state = api.getMe().voiceState
                if (state == null) {
                    offline = true
                } else {
                    available = state.playerAvailable
                    playingSound = state.playingSound
                    offline = false
                    botOffline = state.botOffline
                    channelMismatch = state.channelMissMatch
                    if (!state.botOffline && !state.channelMissMatch) {
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
        when {
            offline -> ErrorText("you off")
            botOffline -> ErrorText("bot off")
            channelMismatch -> ErrorText("Wrong channel")
        }
    }

    if (!loading && !botOffline && !channelMismatch && !offline) {
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
