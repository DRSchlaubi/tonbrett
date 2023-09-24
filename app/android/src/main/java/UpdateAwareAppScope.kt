package dev.schlaubi.tonbrett.app.android

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dev.schlaubi.tonbrett.app.ColorScheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat

private sealed interface State {
    data object Waiting : State
    data object Pending : State
    data class UpdateAvailable(val info: AppUpdateInfo) : State
    data object UpdateDownloaded : State
    data class UpdateDownloading(val percentage: Double) : State
}

@Composable
fun UpdateAwareAppScope(activity: Activity, content: @Composable () -> Unit) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val appUpdateManager =
        remember(context) { AppUpdateManagerFactory.create(context.applicationContext) }
    var progress by remember(appUpdateManager) { mutableStateOf<State>(State.Waiting) }

    val progressListener = remember {
        InstallStateUpdatedListener {
            Log.e("UPDT", "Got new status: ${it.installStatus()}")
            Log.e("UPDT", "Got new status: ${it}")
            when (it.installStatus()) {
                InstallStatus.PENDING -> progress = State.Pending
                InstallStatus.DOWNLOADING -> {
                    progress =
                        State.UpdateDownloading(
                            it.bytesDownloaded().toDouble() / it.totalBytesToDownload().toDouble()
                        )
                }

                InstallStatus.DOWNLOADED -> {
                    progress = State.UpdateDownloaded
                }

                InstallStatus.INSTALLED, InstallStatus.FAILED, InstallStatus.CANCELED -> {
                    progress = State.Waiting
                }

                else -> {}
            }
        }
    }

    LaunchedEffect(appUpdateManager) { appUpdateManager.registerListener(progressListener) }

    if (progress is State.Waiting) {
        LaunchedEffect(appUpdateManager) {
            try {
                val updateInfo = appUpdateManager.appUpdateInfo.await()
                if (updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    progress = State.UpdateAvailable(updateInfo)
                }
            } catch (e: Throwable) {
                Log.w("Tonbrett", "Could not load Update info", e)
            }
        }
    }


    Box(Modifier.fillMaxSize()) {
        content()

        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 10.dp)
                .zIndex(10f)
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 7.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(50.dp)
                    )
            ) {
                @Composable
                fun Info(text: String) {
                    Text(
                        text,
                        style = MaterialTheme.typography.headlineSmall.copy(color = ColorScheme.textColor),
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
                    )

                }

                when (val currentProgress = progress) {
                    is State.Pending -> Info(stringResource(R.string.update_pending))
                    is State.UpdateAvailable -> Button(onClick = {
                        scope.launch {
                            appUpdateManager.startUpdateFlow(
                                currentProgress.info,
                                activity,
                                AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE)
                            ).await()
                        }
                    }) {
                        Info(stringResource(R.string.update_available))
                    }

                    is State.UpdateDownloading -> Info(
                        stringResource(
                            R.string.update_downloading,
                            NumberFormat.getPercentInstance().format(currentProgress.percentage)
                        )
                    )

                    is State.UpdateDownloaded -> Button(onClick = {
                        scope.launch {
                            appUpdateManager.completeUpdate().await()
                        }
                    }) {
                        Icon(Icons.Default.OpenInNew, null)
                        Info(stringResource(R.string.update_download_done))
                    }

                    else -> {} // ignore
                }
            }
        }
    }
}