package dev.schlaubi.tonbrett.app.android.wear.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import dev.schlaubi.tonbrett.app.android.wear.R
import dev.schlaubi.tonbrett.app.api.getUrl
import dev.schlaubi.tonbrett.client.Tonbrett
import dev.schlaubi.tonbrett.common.Id
import dev.schlaubi.tonbrett.common.InterfaceAvailabilityChangeEvent
import dev.schlaubi.tonbrett.common.Sound
import dev.schlaubi.tonbrett.common.SoundCreatedEvent
import dev.schlaubi.tonbrett.common.SoundDeletedEvent
import dev.schlaubi.tonbrett.common.SoundUpdatedEvent
import dev.schlaubi.tonbrett.common.User
import dev.schlaubi.tonbrett.common.VoiceStateUpdateEvent
import kotlinx.coroutines.flow.collectLatest

sealed interface State {
    data object Awaiting : State

    data class Player(
        val available: Boolean,
        val playingSound: Id<Sound>?
    ) : State

    data object Offline : State

    data object ChannelMismatch : State

}

@Composable
fun WearTonbrettApp(token: String) {
    val api = remember(token) { Tonbrett(token, getUrl()) }

    var state by remember { mutableStateOf<State>(State.Awaiting) }
    var sounds by remember { mutableStateOf(emptyList<Sound>()) }

    fun handleVoiceStateUpdate(voiceState: User.VoiceState?) {
        state = if (voiceState == null) {
            State.Offline
        } else if (voiceState.channelMismatch) {
            State.ChannelMismatch
        } else {
            State.Player(voiceState.playerAvailable, voiceState.playingSound)
        }
    }

    LaunchedEffect(api) {
        api.connect()
    }

    if (state is State.Awaiting) {
        LaunchedEffect(api) {
            sounds = api.getSoundList()

            val me = api.getMe()

            handleVoiceStateUpdate(me.voiceState)
        }
    }

    LaunchedEffect(api) {
        api.events.collectLatest {
            when (it) {
                is InterfaceAvailabilityChangeEvent -> {
                    val currentState = state as? State.Player ?: return@collectLatest
                    state = currentState.copy(
                        available = it.available,
                        playingSound = it.playingSongId
                    )
                }

                is SoundCreatedEvent -> sounds += it.sound
                is SoundDeletedEvent -> sounds = sounds.filter { sound -> sound.id != it.id }
                is SoundUpdatedEvent -> {
                    val copy = sounds.toMutableList()
                    copy[copy.indexOfFirst { sound -> sound.id == it.sound.id }] = it.sound
                    sounds = copy
                }

                is VoiceStateUpdateEvent -> handleVoiceStateUpdate(it.voiceState)
            }
        }
    }

    val currentState = state
    if (currentState is State.Player) {
        SoundList(api, currentState, sounds)
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            when (state) {
                is State.Awaiting -> CircularProgressIndicator()
                is State.Offline -> {
                    Info(Icons.Default.OfflineBolt, R.string.voice_state_offline)
                }

                is State.ChannelMismatch -> {
                    Info(Icons.Default.Error, R.string.voice_state_channel_mismatch)
                }

                else -> error("Impossible branch")
            }
        }
    }
}

@Composable
private fun Info(icon: ImageVector, @StringRes id: Int) {
    Icon(icon, null)
    Text(stringResource(id), textAlign = TextAlign.Center)
}
