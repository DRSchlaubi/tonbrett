package dev.schlaubi.tonbrett.app.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.Flow

val LocalSuggestionComposition =
    compositionLocalOf<SuggestionItemComposition> { throw UnsupportedOperationException("No default") }

val LocalEnterPressFlow = compositionLocalOf<Flow<Unit>> { throw UnsupportedOperationException("No default") }

data class SuggestionScope(
    val index: Int,
    val mouseInput: Int,
    val keyboardInput: Int
) {
    val selected: Boolean
        get() = (mouseInput == index) || (keyboardInput == index)
}

data class SuggestionItemComposition(
    var currentIndex: Int,
    val mouseInput: Int,
    val keyboardInput: Int
) {
    fun getAndIncrement() = currentIndex++
}

@Composable
fun SuggestionScope(mouseInput: Int, keyboardInput: Int, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalSuggestionComposition provides SuggestionItemComposition(0, mouseInput, keyboardInput),
        content = content
    )
}

@Composable
fun ProvideEnterPressFlow(flow: Flow<Unit>, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalEnterPressFlow provides flow,
        content = content
    )
}

@Composable
fun SuggestionItem(block: @Composable SuggestionScope.() -> Unit) {
    val scope = LocalSuggestionComposition.current
    block(SuggestionScope(scope.getAndIncrement(), scope.mouseInput, scope.keyboardInput))
}
