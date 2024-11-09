package dev.schlaubi.tonbrett.app.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.schlaubi.tonbrett.app.ColorScheme
import dev.schlaubi.tonbrett.app.LocalStrings
import dev.schlaubi.tonbrett.app.TonbrettViewModel
import dev.schlaubi.tonbrett.app.api.IO
import dev.schlaubi.tonbrett.app.api.LocalContext
import dev.schlaubi.tonbrett.client.Tonbrett
import dev.schlaubi.tonbrett.common.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext

private val LOG = KotlinLogging.logger {}

data class SoundListState(
    val playingSound: Id<Sound>? = null,
    val sounds: List<SoundGroup> = emptyList(),
    val available: Boolean = false,
    val channelMismatch: Boolean = false,
    val offline: Boolean = false
) {
    constructor(voiceState: User.VoiceState?) : this(
        available = voiceState?.playerAvailable == true,
        channelMismatch = voiceState?.channelMismatch == true,
        offline = voiceState == null,
    )
}

class SoundListViewModel(
    private val base: TonbrettViewModel,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SoundListState(base.uiState.value.initialUser?.voiceState))
    val uiState = _uiState.asStateFlow()

    suspend fun reportError(exception: Exception) = base.reportError(exception)

    fun updateSounds(to: List<SoundGroup>) {
        _uiState.update { it.copy(sounds = to) }
    }

    fun reload(scope: CoroutineScope, api: Tonbrett) {
        base.updateLoading(true)
        scope.launch(Dispatchers.IO) {
            val sounds = api.getSounds()
            _uiState.update { it.copy(sounds = sounds) }
            base.updateLoading(false)
        }
    }

    fun handle(voiceState: User.VoiceState?) {
        if (voiceState == null) {
            _uiState.update { it.copy(offline = true) }
        } else {
            _uiState.update {
                it.copy(
                    available = voiceState.playerAvailable,
                    playingSound = voiceState.playingSound,
                    offline = false,
                    channelMismatch = voiceState.channelMismatch
                )
            }
        }
    }

    fun handleEvent(event: Event) {
        val currentState = _uiState.value
        when (event) {
            is VoiceStateUpdateEvent -> handle(event.voiceState)
            is InterfaceAvailabilityChangeEvent -> {
                _uiState.update { it.copy(available = event.available, playingSound = event.playingSongId) }
            }

            is SoundDeletedEvent -> {
                val newSounds = currentState.sounds.mapNotNull {
                    it.copy(sounds = it.sounds.filter { sound -> sound.id != event.id })
                        // Remove group if it is empty
                        .takeIf { group -> group.sounds.isNotEmpty() }
                }
                _uiState.update { it.copy(sounds = newSounds) }
            }

            is SoundCreatedEvent -> {
                val existingGroup = currentState.sounds.firstOrNull { it.tag == event.sound.tag }
                val newSounds = if (existingGroup == null) {
                    currentState.sounds + SoundGroup(event.sound.tag, listOf(event.sound))
                } else {
                    val id = currentState.sounds.indexOf(existingGroup)
                    currentState.sounds.toMutableList().also { copy ->
                        copy[id] = existingGroup.copy(sounds = existingGroup.sounds + event.sound)
                    }
                }
                _uiState.update { it.copy(sounds = newSounds) }
            }

            is SoundUpdatedEvent -> {
                val currentSoundTag = currentState.sounds
                    .first { it.sounds.any { sound -> sound.id == event.sound.id } }
                    .tag
                val groupsCopy = currentState.sounds.toMutableList()
                val currentGroup = groupsCopy.first { it.tag == currentSoundTag }
                if (currentSoundTag == event.sound.tag) {
                    val copy = currentGroup.sounds.toMutableList()
                    copy[copy.indexOfFirst { it.id == event.sound.id }] = event.sound
                    groupsCopy[groupsCopy.indexOf(currentGroup)] = currentGroup.copy(sounds = copy)
                    _uiState.update { it.copy(sounds = groupsCopy) }
                } else {
                    // This is way easier than implementing this logic twice
                    handleEvent(SoundDeletedEvent(event.sound.id))
                    handleEvent(SoundCreatedEvent(event.sound))
                }
            }

            else -> LOG.warn { "Unknown event type: $event" }
        }
    }


    fun startEventHandler(scope: CoroutineScope, api: Tonbrett) {
        api.events
            .onEach { event -> handleEvent(event) }
            .launchIn(scope + Dispatchers.IO)
    }
}

@Composable
fun SoundList(
    baseModel: TonbrettViewModel,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    model: SoundListViewModel = viewModel { SoundListViewModel(baseModel) }
) {
    val baseState by baseModel.uiState.collectAsState()
    val api = LocalContext.current.api

    LaunchedEffect(Unit) {
        model.startEventHandler(coroutineScope, api)
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            model.reload(coroutineScope, api)
        }
    }


    AnimatedVisibility(!baseState.loading, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.fillMaxSize()) {
        SoundContainer(model)
    }
}
