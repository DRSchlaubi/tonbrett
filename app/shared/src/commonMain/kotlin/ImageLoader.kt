package dev.schlaubi.tonbrett.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun OptionalWebImage(url: String?, contentDescription: String? = null, modifier: Modifier = Modifier) = OptionalWebImageInternal(url, contentDescription, modifier)

@Composable
expect fun OptionalWebImageInternal(url: String?, contentDescription: String?, modifier: Modifier)
