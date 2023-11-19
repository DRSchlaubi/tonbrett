package dev.schlaubi.tonbrett.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Currently no auto update, due to: https://github.com/JetBrains/compose-multiplatform/issues/1986
@Composable
expect fun isSystemInDarkMode(): Boolean

sealed interface ColorScheme {
    val container: Color
    val secondaryContainer: Color
    val searchBarColor: Color
    val textColor: Color
    val active: Color
    val disabled: Color
    val blurple: Color
    val error: Color
    
    companion object {
        val current: ColorScheme
            @Composable
            get() = if (isSystemInDarkMode()) DarkColorScheme else LightColorTheme
    }
}

private object DarkColorScheme : ColorScheme {
    override val container = Color(17, 18, 20)
    override val secondaryContainer = Color(43, 45, 49)
    override val searchBarColor = Color(30, 31, 34)
    override val textColor = Color.White
    override val active = Color(87, 242, 135)
    override val disabled = Color.LightGray
    override val blurple = Color(88, 101, 242)
    override val error = Color(237, 66, 69)
}

private object LightColorTheme : ColorScheme {
    override val container = Color(255, 255, 255)
    override val secondaryContainer = Color(242, 243, 245)
    override val searchBarColor = Color(227, 229, 232)
    override val textColor = Color.Black
    override val active = Color(87, 242, 135)
    override val disabled = Color.DarkGray
    override val blurple = Color(88, 101, 242)
    override val error = Color(237, 66, 69)
}
