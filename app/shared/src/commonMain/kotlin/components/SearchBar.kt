package dev.schlaubi.tonbrett.app.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ripple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import cafe.adriel.lyricist.LocalStrings
import dev.schlaubi.tonbrett.app.ColorScheme
import dev.schlaubi.tonbrett.app.api.IO
import dev.schlaubi.tonbrett.app.api.LocalContext
import dev.schlaubi.tonbrett.common.SoundGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

typealias SoundUpdater = (List<SoundGroup>) -> Unit

private val protectedKeys = listOf(Key.Enter, Key.DirectionUp, Key.DirectionDown)

@OptIn(FlowPreview::class)
@Composable
fun SearchBar(updateSounds: SoundUpdater) {
    var value by remember { mutableStateOf("") }
    val enterPresses = remember { MutableSharedFlow<Unit>() }
    var maxSuggestions by remember { mutableStateOf(0) }
    val strings = LocalStrings.current
    val api = LocalContext.current.api
    var showSuggestions by remember { mutableStateOf(false) }
    var selectedSuggestion by remember(showSuggestions) { mutableStateOf(-1) }
    val scope = rememberCoroutineScope()
    val updates = remember { MutableStateFlow(value) }

    fun updateSearch(to: String) {
        updates.tryEmit(to)
        value = to
    }

    fun showSuggestions(to: Boolean) {
        showSuggestions = to
    }

    fun updateMaxSuggestionsTo(to: Int) {
        maxSuggestions = to
    }

    LaunchedEffect(Unit) {
        updates
            .debounce(300.milliseconds)
            .onEach {
                withContext(Dispatchers.IO) {
                    updateSounds(api.getSounds(false, it.ifBlank { null }))
                }
            }
            .launchIn(scope)
    }

    // Capture key events higher up in the chain so we can pass them down to children
    Column(Modifier.onPreviewKeyEvent {
        if (it.type != KeyEventType.KeyUp) return@onPreviewKeyEvent it.key in protectedKeys
        selectedSuggestion = when (it.key) {
            Key.DirectionUp -> (selectedSuggestion - 1).coerceAtLeast(0)
            Key.DirectionDown -> (selectedSuggestion + 1).coerceAtMost(maxSuggestions)
            Key.Enter -> {
                return@onPreviewKeyEvent if (selectedSuggestion >= 0) {
                    scope.launch {
                        enterPresses.emit(Unit)
                    }
                    true
                } else {
                    false
                }
            }

            Key.Escape -> {
                showSuggestions(false)
                -1
            } // reset selection until menu reopens
            else -> return@onPreviewKeyEvent false
        }
        true
    }) {
        DockedSearchBar(
            {
                SearchBarDefaults.InputField(
                    value, ::updateSearch, { showSuggestions(false) },
                    showSuggestions, ::showSuggestions,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp),
                    trailingIcon = {
                        TrailingIcon(value, {
                            updateSearch(it)
                        }, false)
                    },
                    placeholder = {
                        Text(
                            strings.searchExplainer,
                            color = ColorScheme.current.textColor.copy(alpha = .7f)
                        )
                    },
                    colors = TextFieldDefaults.colors(unfocusedTextColor = ColorScheme.current.textColor, focusedTextColor = ColorScheme.current.textColor)
                )
            },
            showSuggestions, ::showSuggestions,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp),
            colors = SearchBarDefaults.colors(containerColor = ColorScheme.current.searchBarColor)

        ) {
            ProvideEnterPressFlow(enterPresses) {
                SearchSuggestions(
                    value,
                    -1,
                    ::updateSearch,
                    ::showSuggestions,
                    ::updateMaxSuggestionsTo
                )
            }

            BoxWithConstraints(
                Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                val density = LocalDensity.current
                val height = with(density) { constraints.maxHeight.toDp() }

                if (height > 100.dp) {
                    Text(strings.typeForMoreSuggestions, color = ColorScheme.current.textColor)
                }
            }
        }
    }
}

@Composable
private fun TrailingIcon(value: String, updateSearch: (String) -> Unit, supportsVoiceInput: Boolean) {
    val isSearching = value.isNotEmpty()
    val transition = updateTransition(targetState = isSearching)
    val iconRotation by transition.animateFloat { searching ->
        if (searching) 90f else 0f
    }
    val interactionSource = remember { MutableInteractionSource() }

    val icon = if (value.isEmpty()) {
        if (supportsVoiceInput) {
            Icons.Default.Mic
        } else {
            Icons.Default.Search
        }
    } else {
        Icons.Default.Clear
    }

    Icon(icon, null, Modifier
        .rotate(iconRotation)
        .clickable(interactionSource, indication = ripple(), enabled = value.isNotEmpty()) {
            updateSearch("")
        }
    )
}
