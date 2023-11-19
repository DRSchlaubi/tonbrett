package dev.schlaubi.tonbrett.app.android

import android.app.Activity
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
import androidx.compose.runtime.collectAsState
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
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.ktx.AppUpdateResult
import com.google.android.play.core.ktx.bytesDownloaded
import com.google.android.play.core.ktx.installStatus
import com.google.android.play.core.ktx.requestCompleteUpdate
import com.google.android.play.core.ktx.requestUpdateFlow
import com.google.android.play.core.ktx.totalBytesToDownload
import dev.schlaubi.tonbrett.app.ColorScheme
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.NumberFormat

@Composable
fun UpdateAwareAppScope(activity: Activity, content: @Composable () -> Unit) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val appUpdateManager =
        remember(context) { AppUpdateManagerFactory.create(context.applicationContext) }

    val progressFlow = remember(appUpdateManager) {
        appUpdateManager.requestUpdateFlow()
            .catch { emit(AppUpdateResult.NotAvailable) }
    }
    val progress: AppUpdateResult? by progressFlow.collectAsState(initial = null)
    var failed by remember(appUpdateManager) { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        content()

        if (failed) return

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
                        style = MaterialTheme.typography.headlineSmall.copy(color = ColorScheme.current.textColor),
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
                    )
                }

                when (val currentProgress = progress) {
                    is AppUpdateResult.InProgress -> {
                        val installState = currentProgress.installState
                        when (installState.installStatus) {
                            InstallStatus.CANCELED, InstallStatus.INSTALLED, InstallStatus.FAILED -> {
                                failed = true
                            }

                            InstallStatus.DOWNLOADING -> {
                                val ratio = installState.bytesDownloaded.toDouble() /
                                        installState.totalBytesToDownload.toDouble()
                                            .coerceAtLeast(1.0) // avoid division by zero
                                val percentage = NumberFormat.getPercentInstance().format(ratio)
                                Info(stringResource(R.string.update_downloading, percentage))
                            }

                            InstallStatus.PENDING -> {
                                Info(stringResource(R.string.update_pending))
                            }

                            else -> {} // ignore
                        }
                    }

                    is AppUpdateResult.Available -> Button(onClick = {
                        currentProgress.startFlexibleUpdate(activity, 12548)
                    }) {
                        Info(stringResource(R.string.update_available))
                    }

                    is AppUpdateResult.Downloaded -> Button(onClick = {
                        scope.launch {
                            appUpdateManager.requestCompleteUpdate()
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