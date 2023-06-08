package dev.schlaubi.tonbrett.app.android

import android.app.Activity
import android.os.Bundle
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
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dev.schlaubi.tonbrett.app.ColorScheme
import dev.schlaubi.tonbrett.app.strings.LocalStrings
import kotlinx.coroutines.tasks.await

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
            updateInfo = appUpdateManager.appUpdateInfo.await()
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
        if (currentInfo == null) {
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