//package dev.schlaubi.tonbrett.app.android
//
//import android.util.Log
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.ui.platform.LocalContext
//import com.google.android.horologist.annotations.ExperimentalHorologistApi
//import com.google.android.horologist.auth.data.phone.tokenshare.impl.TokenBundleRepositoryImpl
//import com.google.android.horologist.data.WearDataLayerRegistry
//import dev.schlaubi.tonbrett.app.android.shared.AUTH_STATE_PATH
//import dev.schlaubi.tonbrett.app.android.shared.AuthStateSerializer
//import dev.schlaubi.tonbrett.app.android.shared.authState
//
//@OptIn(ExperimentalHorologistApi::class)
//@Composable
//fun WearOSTokenSharing(token: String?) {
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//    val registry = remember(context, coroutineScope) {
//        WearDataLayerRegistry.fromContext(
//            context.applicationContext,
//            coroutineScope
//        )
//    }
//    val repository = remember(registry) {
//        TokenBundleRepositoryImpl(
//            registry = registry,
//            coroutineScope = coroutineScope,
//            key = AUTH_STATE_PATH,
//            serializer = AuthStateSerializer
//        )
//    }
//
//    LaunchedEffect(token) {
//        Log.d("W", "Is repo available: ${repository.isAvailable()}")
//        if (repository.isAvailable() && token != null) {
//            Log.d("W", "Sending")
//            val state = authState {
//                this.token = token
//            }
//            repository.update(state)
//        }
//    }
//}
