package dev.schlaubi.tonbrett.app.android.wear

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text

class WearActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val authState by rememberAuthState()

            Log.d("E", authState.toString())
            Log.d("E", "Has auth State: ${authState.hasToken()}")
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                if (authState.hasToken()) {
                    Text("Current token: ${authState.token}")
                } else {
                    CircularProgressIndicator()
                }
            }
        }
    }
}