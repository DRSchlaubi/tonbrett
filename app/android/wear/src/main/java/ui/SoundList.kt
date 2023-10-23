package dev.schlaubi.tonbrett.app.android.wear.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import coil.compose.AsyncImage
import dev.schlaubi.tonbrett.app.util.conditional
import dev.schlaubi.tonbrett.client.Tonbrett
import dev.schlaubi.tonbrett.common.Sound
import kotlinx.coroutines.launch

@Composable
fun SoundList(api: Tonbrett, state: State.Player, sounds: List<Sound>) {
    ScalingLazyColumn {
        items(sounds) {
            SoundCard(api, it, state.playingSound == it.id, state.available)
        }
    }
}

@Composable
private fun SoundCard(api: Tonbrett, sound: Sound, playing: Boolean, canStop: Boolean) {
    val coroutineScope = rememberCoroutineScope()
    Card(
        onClick = {
            coroutineScope.launch {
                if (playing && canStop) {
                    api.stop()
                } else if (!playing) {
                    api.play(sound.id.toString())
                }
            }
        },
        role = Role.Button,
        modifier = Modifier.conditional(playing) {
            border(3.dp, Color.Green, MaterialTheme.shapes.large)
        }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            val emoji = sound.emoji
            if (emoji is Sound.Emoji.HasUrl) {
                AsyncImage(model = emoji.url, null)
            }

            Text(sound.name)
        }
    }
}
