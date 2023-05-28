package dev.schlaubi.tonbrett.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import dev.schlaubi.tonbrett.app.ColorScheme
import dev.schlaubi.tonbrett.app.api.IO
import dev.schlaubi.tonbrett.app.api.LocalContext
import dev.schlaubi.tonbrett.app.strings.LocalStrings
import dev.schlaubi.tonbrett.app.util.canClearFocus
import dev.schlaubi.tonbrett.common.Sound
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

typealias SoundUpdater = (List<Sound>) -> Unit

@OptIn(ExperimentalComposeUiApi::class)
private val protectedKeys = listOf(Key.Enter, Key.DirectionUp, Key.DirectionDown)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchBarScope(updateSounds: SoundUpdater, content: @Composable () -> Unit) {
    var onlineMine by remember { mutableStateOf(false) }
    var value by remember { mutableStateOf(TextFieldValue("")) }
    val enterPresses = remember { MutableSharedFlow<Unit>() }
    var maxSuggestions by remember { mutableStateOf(0) }
    val strings = LocalStrings.current
    var showSuggestions by remember { mutableStateOf(false) }
    var selectedSuggestion by remember(showSuggestions) { mutableStateOf(-1) }
    val scope = rememberCoroutineScope()

    fun updateOnlineMine(to: Boolean) {
        onlineMine = to
    }

    fun updateSearch(to: TextFieldValue) {
        value = to
    }

    fun showSuggestions(to: Boolean) {
        showSuggestions = to
    }

    fun updateMaxSuggestionsTo(to: Int) {
        maxSuggestions = to
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

            Key.Escape -> -1 // reset selection until menu reopens
            else -> return@onPreviewKeyEvent false
        }
        true
    }) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 15.dp)
        ) {
            SearchField(value, onlineMine, updateSounds, ::updateSearch, showSuggestions, ::showSuggestions)
            Spacer(Modifier.padding(horizontal = 5.dp).canClearFocus())
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.canClearFocus().fillMaxWidth()
            ) {
                OnlineMineCheckbox(onlineMine, value.text, updateSounds, ::updateOnlineMine)
                BoxWithConstraints {
                    if (maxWidth >= 30.dp) {
                        Spacer(Modifier.padding(horizontal = 2.dp))
                        Text(strings.onlineMine, color = ColorScheme.textColor)
                    } else {
                        Icon(Icons.Default.Person, strings.onlineMine, tint = ColorScheme.textColor)
                    }
                }
            }
        }
        BoxWithConstraints {
            content()
            if (showSuggestions) {
                ProvideEnterPressFlow(enterPresses) {
                    SearchSuggestions(
                        value,
                        selectedSuggestion,
                        ::updateSearch,
                        ::showSuggestions,
                        ::updateMaxSuggestionsTo
                    )
                }
            }
        }
    }
}

@OptIn(FlowPreview::class, ExperimentalComposeUiApi::class)
@Composable
private fun SearchField(
    value: TextFieldValue, onlyMine: Boolean, updateSounds: SoundUpdater,
    updateSearch: (TextFieldValue) -> Unit,
    showSuggestions: Boolean,
    updateShowSuggestions: (Boolean) -> Unit
) {
    val updates = remember { MutableStateFlow(value) }
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current
    val api = LocalContext.current.api
    val focusManager = LocalFocusManager.current

    suspend fun handleInput(input: TextFieldValue) {
        val text = input.text
        if (!input.annotatedString.hasStringAnnotations(skipSuggestionProcessing, 0, 0)
            && !showSuggestions && text.startsWith("tag:")
        ) {
            updateShowSuggestions(true)
        }
        updates.emit(input)
    }

    LaunchedEffect(onlyMine) {
        updates
            .debounce(300.milliseconds)
            .onEach {
                withContext(Dispatchers.IO) {
                    updateSounds(api.getSounds(onlyMine, it.text.ifBlank { null }))
                }
            }
            .launchIn(scope)
    }

    // Handle incoming changes from everywhere
    LaunchedEffect(value.text) {
        handleInput(value)
    }

    OutlinedTextField(
        value,
        updateSearch,
        placeholder = { Text(strings.searchExplainer) },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = ColorScheme.searchBarColor,
            placeholderColor = ColorScheme.secondaryContainer,
            textColor = ColorScheme.textColor,
            focusedBorderColor = ColorScheme.searchBarColor,
            disabledBorderColor = ColorScheme.searchBarColor,
            errorBorderColor = ColorScheme.searchBarColor,
            unfocusedBorderColor = ColorScheme.searchBarColor
        ),
        shape = RoundedCornerShape(10.dp),
        trailingIcon = {
            TrailingIcon(value) {
                scope.launch {
                    updates.emit(it)
                }
                updateSearch(it)
            }
        },
        singleLine = true,
        visualTransformation = SyntaxHighlightingTextTransformation,
        modifier = Modifier.fillMaxWidth(.8f)
            .onFocusChanged { updateShowSuggestions(it.hasFocus) }
            .focusRequester(remember { FocusRequester() })
            .background(color = Color.Companion.Transparent, shape = RoundedCornerShape(25.dp))
            .onPreviewKeyEvent {
                when (it.key) {
                    Key.Escape, Key.Enter -> focusManager.clearFocus()
                    else -> return@onPreviewKeyEvent false
                }
                true
            }
    )
}

@Composable
private fun OnlineMineCheckbox(
    checked: Boolean,
    search: String,
    updateSounds: SoundUpdater,
    updateValue: (Boolean) -> Unit
) {
    var disabled by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val api = LocalContext.current.api

    fun update(to: Boolean) {
        if (disabled) return
        disabled = true
        updateValue(to)
        scope.launch(Dispatchers.IO) {
            val newSounds = api.getSounds(query = search, onlyMine = to)
            updateSounds(newSounds)
            disabled = false
        }
    }

    Checkbox(
        checked, ::update,
        colors = CheckboxDefaults.colors(
            checkedColor = if (disabled) ColorScheme.secondaryContainer else ColorScheme.blurple
        )
    )
}

@Composable
private fun TrailingIcon(value: TextFieldValue, updateSearch: (TextFieldValue) -> Unit) {
    if (value.text.isEmpty()) {
        Icon(Icons.Default.Search, null)
    } else {
        IconButton({ updateSearch(TextFieldValue("")) }) {
            Icon(Icons.Default.Clear, null)
        }
    }
}
