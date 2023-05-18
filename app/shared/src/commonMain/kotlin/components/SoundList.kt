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
            .onEach {
                when (it) {
                    is VoiceStateUpdateEvent -> reload(it.voiceState)
                    is InterfaceAvailabilityChangeEvent -> {
                        playingSound = it.playingSongId
                        available = it.available
                    }
                    else -> println("Unknown event type: $it")
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
