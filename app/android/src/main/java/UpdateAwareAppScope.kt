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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun UpdateAwareAppScope(activity: Activity, content: @Composable () -> Unit) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val appUpdateManager =
        remember(context) { AppUpdateManagerFactory.create(context.applicationContext) }
    var updateInfo by remember(appUpdateManager) { mutableStateOf<AppUpdateInfo?>(null) }
    var progress by remember(updateInfo) { mutableStateOf<Double?>(null) }

    val progressListener = remember {
        InstallStateUpdatedListener {
            when (it.installStatus()) {
                InstallStatus.DOWNLOADING -> {
                    progress =
                        it.bytesDownloaded().toDouble() / it.totalBytesToDownload().toDouble()
                }

                InstallStatus.DOWNLOADED -> {
                    progress = 2.0
                }

                else -> {}
            }
        }
    }

    SideEffect { appUpdateManager.registerListener(progressListener) }

    if (updateInfo == null) {
        LaunchedEffect(appUpdateManager) {
            try {
                updateInfo = appUpdateManager.appUpdateInfo.await()
            } catch (e: Throwable) {
                Log.w("Tonbrett", "Could not load Update info", e)
            }
        }
    }


    Box(Modifier.fillMaxSize()) {
        content()

        val currentUpdateInfo = updateInfo
        if(currentUpdateInfo != null) {
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
                        .background(MaterialTheme.colorScheme.onSecondary, RoundedCornerShape(50.dp))
                ) {
                    val currentProgress = progress ?: -1.0

                    @Composable
                    fun Info(text: String) {
                        Text(
                            text,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
                        )

                    }

                    if (currentProgress < 0) {
                        Button(onClick = { scope.launch {
                            appUpdateManager.startUpdateFlow(
                                currentUpdateInfo,
                                activity,
                                AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE)
                            )
                        } }) {
                            Info("Update Available")
                        }
                    } else if (currentProgress in 0.0..1.0) {
                        Info("Updating")
                    } else {
                        Button(onClick = {
                            scope.launch {
                                appUpdateManager.completeUpdate().await()
                            }
                        }) {
                            Icon(Icons.Default.OpenInNew, null)
                            Info("Update Available")
                        }
                    }
                }
            }
        }
    }
}