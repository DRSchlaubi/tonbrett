package dev.schlaubi.tonbrett.app.android.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import dev.schlaubi.tonbrett.app.android.wear.ui.WearTonbrettApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val authState by rememberAuthState()

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                val currentAuthState = authState
                if (currentAuthState == null) {
                    CircularProgressIndicator()
                } else if (currentAuthState.hasToken()) {
                    WearTonbrettApp(currentAuthState.token)
                } else {
                    Icon(Icons.Default.PhoneAndroid, null)
                    Text(stringResource(R.string.sign_in))
                    CircularProgressIndicator()
                }
            }
        }
    }
}