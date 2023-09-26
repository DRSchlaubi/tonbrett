package dev.schlaubi.tonbrett.app.android

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.lyricist.LocalStrings
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestAppUpdateInfo
import dev.schlaubi.tonbrett.app.ColorScheme

class UpdateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { UpdateScreen(this) }
    }
}

@Composable
fun UpdateScreen(activity: Activity) {
    val appUpdateManager =
        remember(activity) { AppUpdateManagerFactory.create(activity.applicationContext) }
    var updateInfo by remember(appUpdateManager) { mutableStateOf<AppUpdateInfo?>(null) }
    var renderFallbackUpdate by remember { mutableStateOf(false) }
    val strings = LocalStrings.current

    fun showUpdateInfo(updateInfo: AppUpdateInfo) {
        appUpdateManager.startUpdateFlow(
            updateInfo,
            activity,
            AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE)
        )
    }

    if (updateInfo == null) {
        LaunchedEffect(appUpdateManager) {
            try {
                updateInfo = appUpdateManager.requestAppUpdateInfo()
            } catch (e: Throwable) {
                Log.w("Tonbrett", "Could not load Update info", e)
                renderFallbackUpdate = true
            }
        }
    } else {
        LaunchedEffect(Unit) { showUpdateInfo(updateInfo!!) }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            strings.appCrash,
            color = ColorScheme.textColor,
            style = MaterialTheme.typography.headlineMedium
        )
        val currentInfo = updateInfo
        if (renderFallbackUpdate) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/DRSchlaubi/tonbrett/releases/latest")
            )
            Button({ activity.startActivity(intent) }) {
                Icon(Icons.Default.Refresh, null)
                Text(strings.update)
            }
        } else if (currentInfo == null) {
            CircularProgressIndicator()
        } else if (currentInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
            Button({ showUpdateInfo(currentInfo) }) {
                Icon(Icons.Default.Refresh, null)
                Text(strings.update)
            }
        } else {
            Text(strings.unknownError)
        }
    }
}