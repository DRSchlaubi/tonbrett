package dev.schlaubi.tonbrett.app.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.schlaubi.tonbrett.app.ColorScheme
import dev.schlaubi.tonbrett.app.api.IO
import dev.schlaubi.tonbrett.app.api.LocalContext
import dev.schlaubi.tonbrett.app.strings.LocalStrings
import dev.schlaubi.tonbrett.common.Sound
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

typealias SoundUpdater = (List<Sound>) -> Unit

@Composable
fun SearchBar(updateSounds: SoundUpdater) {
    var onlineMine by remember { mutableStateOf(false) }
    var value by remember { mutableStateOf("") }
    val strings = LocalStrings.current

    fun updateOnlineMine(to: Boolean) {
        onlineMine = to
    }

    fun updateSearch(to: String) {
        value = to
    }

    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier.padding(vertical = 10.dp, horizontal = 15.dp)
    ) {
        SearchField(value, onlineMine, updateSounds, ::updateSearch)
        Spacer(Modifier.padding(horizontal = 5.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OnlineMineCheckbox(onlineMine, updateSounds, ::updateOnlineMine)
            Spacer(Modifier.padding(horizontal = 2.dp))
            Text(strings.onlineMine, color = ColorScheme.textColor)
        }
    }
}

@OptIn(FlowPreview::class)
@Composable
private fun SearchField(value: String, onlyMine: Boolean, updateSounds: SoundUpdater, updateSearch: (String) -> Unit) {
    val updates = remember { MutableStateFlow(value) }
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current
    val api = LocalContext.current.api

    fun handleInput(input: String) {
        updateSearch(input)
        scope.launch {
            updates.emit(input)
        }
    }

    LaunchedEffect(Unit) {
        updates
            .debounce(300.milliseconds)
            .onEach {
                withContext(Dispatchers.IO) {
                    updateSounds(api.getSounds(onlyMine, it.ifBlank { null }))
                }
            }
            .launchIn(scope)
    }

    OutlinedTextField(
        value,
        ::handleInput,
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
        trailingIcon = { Icon(Icons.Default.Search, strings.search) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(.8f)
    )

}

@Composable
private fun OnlineMineCheckbox(checked: Boolean, updateSounds: SoundUpdater, updateValue: (Boolean) -> Unit) {
    var disabled by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val api = LocalContext.current.api

    fun update(to: Boolean) {
        if (disabled) return
        disabled = true
        updateValue(to)
        scope.launch(Dispatchers.IO) {
            val newSounds = api.getSounds(onlyMine = to)
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
