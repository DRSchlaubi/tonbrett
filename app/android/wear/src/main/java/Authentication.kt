package dev.schlaubi.tonbrett.app.android.wear

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.auth.data.tokenshare.impl.TokenBundleRepositoryImpl
import com.google.android.horologist.data.WearDataLayerRegistry
import dev.schlaubi.tonbrett.app.android.shared.AUTH_STATE_PATH
import dev.schlaubi.tonbrett.app.android.shared.AuthState
import dev.schlaubi.tonbrett.app.android.shared.AuthStateSerializer

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun rememberAuthState(): State<AuthState?> {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val registry = remember(context, coroutineScope) {
        WearDataLayerRegistry.fromContext(
            context,
            coroutineScope
        )
    }
    val repository = remember(registry) {
        TokenBundleRepositoryImpl(
            registry,
            AuthStateSerializer,
            AUTH_STATE_PATH
        )
    }

    return repository.flow.collectAsState(initial = null)
}