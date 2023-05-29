package dev.schlaubi.tonbrett.app.android

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.schlaubi.tonbrett.app.api.AppContext
import dev.schlaubi.tonbrett.app.api.LocalContext
import dev.schlaubi.tonbrett.app.api.getUrl
import dev.schlaubi.tonbrett.app.api.tokenKey
import dev.schlaubi.tonbrett.common.Route
import dev.schlaubi.tonbrett.client.href
import io.ktor.http.URLBuilder

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = AppContext(applicationContext)
        if (intent.data?.scheme == "tonbrett" && intent.data?.host == "login") {
            val token = intent.data?.getQueryParameter("token")
            if (token != null) {
                context.vault.set(tokenKey, token)
                return startApp()
            }
        }
        if (context.getTokenOrNull() == null) {
            setContent {
                TonbrettAuthorizationScreenTheme {
                    AuthorizationScreen()
                }
            }
        } else {
            startApp()
        }
    }

    private fun launchAuthorization() {
        val urlBuilder = URLBuilder(getUrl())
        href(Route.Auth(Route.Auth.Type.MOBILE_APP), urlBuilder)
        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(this, Uri.parse(urlBuilder.buildString()))
    }

    private fun startApp() {
        startActivity(Intent(this, AppActivity::class.java))
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun AuthorizationScreen() {
        SideEffect { launchAuthorization() }

        MaterialTheme {
            Scaffold(containerColor = MaterialTheme.colorScheme.primaryContainer) { padding ->
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Button(onClick = ::launchAuthorization) {
                        Icon(Icons.Default.Refresh, stringResource(id = R.string.retry))
                        Text(stringResource(id = R.string.retry))
                    }
                }
            }
        }
    }
}
