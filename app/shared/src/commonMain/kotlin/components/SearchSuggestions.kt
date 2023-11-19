package dev.schlaubi.tonbrett.app.components

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import cafe.adriel.lyricist.LocalStrings
import dev.schlaubi.tonbrett.app.ColorScheme
import dev.schlaubi.tonbrett.app.api.LocalContext
import dev.schlaubi.tonbrett.app.util.conditional
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take

@Composable
fun SearchSuggestions(
    value: String,
    selectedKeyboardSuggestion: Int,
    updateSearch: (String) -> Unit,
    showSuggestions: (Boolean) -> Unit,
    updateMaxSuggestions: (Int) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hoveringOverSuggestions by interactionSource.collectIsHoveredAsState()
    var selectedSuggestion by remember { mutableStateOf(-1) }
    var currentTags by remember { mutableStateOf(emptyList<String>()) }
    val api = LocalContext.current.api
    val strings = LocalStrings.current
    val tagOnlySearch = value.startsWith("tag:")

    fun selectSuggestion(index: Int) {
        selectedSuggestion = index
    }

    if (!hoveringOverSuggestions) {
        selectedSuggestion = -1
    }

    LaunchedEffect(value) {
        currentTags = if (tagOnlySearch || value.isNotEmpty()) {
            val limit = if (tagOnlySearch) 7 else 3
            api.getTags(value.substringAfter("tag:"), limit)
        } else {
            emptyList()
        }
    }

    if (!tagOnlySearch || currentTags.isNotEmpty()) {
        Column(Modifier.padding(vertical = 7.dp, horizontal = 10.dp)) {
            SuggestionScope(selectedSuggestion, selectedKeyboardSuggestion) {
                if (value.isNotBlank() && currentTags.isNotEmpty()) {
                    Text(
                        strings.searchByTag,
                        color = ColorScheme.textColor,
                        modifier = Modifier.padding(horizontal = 5.dp)
                    )
                    currentTags.forEach { tag ->
                        SyntaxSuggestion(
                            "tag",
                            tag,
                            null,
                            ::selectSuggestion,
                            updateSearch,
                            showSuggestions
                        )
                    }
                }
                if (!value.startsWith("tag:")) {
                    if (currentTags.isNotEmpty()) {
                        Box(Modifier.padding(vertical = 10.dp, horizontal = 7.dp)) {
                            Divider(Modifier.fillMaxWidth(), color = Color.DarkGray, thickness = 2.dp)
                        }
                    }
                    Text(
                        strings.searchOptions,
                        color = ColorScheme.textColor,
                        modifier = Modifier.padding(horizontal = 5.dp)
                    )
                    Column(Modifier.hoverable(interactionSource)) {
                        val keywords = mapOf(
                            "name" to strings.searchByName,
                            "tag" to strings.searchByTag,
                            "description" to strings.searchByDescription
                        )

                        keywords.forEach { (name, description) ->
                            SyntaxSuggestion(
                                name,
                                value,
                                description,
                                ::selectSuggestion,
                                updateSearch,
                                showSuggestions
                            )
                        }
                    }

                    updateMaxSuggestions(LocalSuggestionComposition.current.currentIndex)
                }
            }
        }
    }
}

@NonRestartableComposable // during recomposition the index might change
@Composable
private fun SyntaxSuggestion(
    name: String,
    value: String,
    placeholder: String?,
    updateSelected: (Int) -> Unit,
    updateSearch: (String) -> Unit,
    showSuggestions: (Boolean) -> Unit
) = SuggestionItem {
    if (value.startsWith("$name:")) return@SuggestionItem
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val enterFlow = LocalEnterPressFlow.current
    val strings = LocalStrings.current
    if (hovered) {
        updateSelected(index)
    }
    val text = buildAnnotatedString {
        append(AnnotatedString("$name:", SpanStyle(color = ColorScheme.textColor)))
        append(' ')
        val color = if (value.isBlank()) Color.Gray else Color.LightGray
        val text = value.ifBlank { placeholder }?.substringAfter(':')
        checkNotNull(text) { "Text can only be null if a value is always specified" }
        append(AnnotatedString(text, SpanStyle(color = color)))
    }

    fun select() {
        val actualText = value.substringAfter(":")
        updateSearch("$name:$actualText")
    }

    if (selected) {
        LaunchedEffect(value) {
            enterFlow
                .take(1)
                .onEach {
                    select()
                    showSuggestions(false)
                }
                .launchIn(this)
        }
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 5.dp)
            .focusable(interactionSource = interactionSource)
            .hoverable(interactionSource)
            .clickable(indication = LocalIndication.current, interactionSource = interactionSource, onClick = ::select)
            .conditional(selected) {
                background(Color.DarkGray, RoundedCornerShape(5.dp))
            }
    ) {
        Text(text, Modifier.padding(horizontal = 5.dp))
        Spacer(Modifier.weight(100f))
        Icon(Icons.Default.Add, strings.enterToAdd, tint = Color.LightGray)
    }
}
