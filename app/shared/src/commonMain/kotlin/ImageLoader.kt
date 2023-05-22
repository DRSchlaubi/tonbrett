package dev.schlaubi.tonbrett.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun OptionalWebImage(url: String?, contentDescription: String? = null, modifier: Modifier = Modifier)

