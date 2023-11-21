package dev.schlaubi.tonbrett.app.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.lyricist.LocalStrings
import dev.schlaubi.tonbrett.app.ErrorReporter
import dev.schlaubi.tonbrett.app.api.IO
import dev.schlaubi.tonbrett.app.api.LocalContext
import dev.schlaubi.tonbrett.common.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging

private val LOG = KotlinLogging.logger {}

@Composable
fun SoundList(errorReporter: ErrorReporter, voiceState: User.VoiceState?) {
    var playingSound by remember { mutableStateOf<Id<Sound>?>(null) }
    var sounds by remember { mutableStateOf<List<SoundGroup>>(emptyList()) }
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
                    fun handleEvent(event: Event) {
                        when (event) {
                            is VoiceStateUpdateEvent -> reload(event.voiceState)
                            is InterfaceAvailabilityChangeEvent -> {
                                playingSound = event.playingSongId
                                available = event.available
                            }

                            is SoundDeletedEvent -> {
                                sounds = sounds.mapNotNull {
                                    it.copy(sounds = it.sounds.filter { sound -> sound.id != event.id })
                                        // Remove group if it is empty
                                        .takeIf { group -> group.sounds.isNotEmpty() }
                                }
                            }

                            is SoundCreatedEvent -> {
                                val existingGroup = sounds.firstOrNull { it.tag == event.sound.tag }
                                if (existingGroup == null) {
                                    sounds += SoundGroup(event.sound.tag, listOf(event.sound))
                                } else {
                                    val id = sounds.indexOf(existingGroup)
                                    val copy = sounds.toMutableList()
                                    copy[id] = existingGroup.copy(sounds = existingGroup.sounds + event.sound)
                                    sounds = copy
                                }
                            }

                            is SoundUpdatedEvent -> {
                                val currentSoundTag = sounds
                                    .first { it.sounds.any { sound -> sound.id == event.sound.id } }
                                    .tag
                                val groupsCopy = sounds.toMutableList()
                                val currentGroup = groupsCopy.first { it.tag == currentSoundTag }
                                if (currentSoundTag == event.sound.tag) {
                                    val copy = currentGroup.sounds.toMutableList()
                                    copy[copy.indexOfFirst { it.id == event.sound.id }] = event.sound
                                    groupsCopy[groupsCopy.indexOf(currentGroup)] = currentGroup.copy(sounds = copy)
                                    sounds = groupsCopy
                                } else {
                                    // This is way easier than implementing this logic twice
                                    handleEvent(SoundDeletedEvent(event.sound.id))
                                    handleEvent(SoundCreatedEvent(event.sound))
                                }
                            }

                            else -> LOG.warn { "Unknown event type: $event" }
                        }
                    }
                    handleEvent(event)
                }
                .launchIn(this)
        }
    }


    if (loading) {
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                sounds = api.getSounds()
                loading = false
            }
        }

        NonListBlock {
            CircularProgressIndicator()
        }
    } else {
        Column {
            val strings = LocalStrings.current
            val availabilityReason = when {
                offline -> strings.offline
                channelMismatch -> strings.wrongChannelExplainer
                !loading && sounds.isEmpty() -> strings.noSounds
                !available -> strings.playerBusy
                else -> null
            }
            AnimatedContent(!loading) { render ->
                if (render) {
                    SoundContainer(sounds, errorReporter, playingSound, availabilityReason) {
                        sounds = it
                    }
                }
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
