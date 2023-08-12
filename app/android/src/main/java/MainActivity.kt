package dev.schlaubi.tonbrett.app.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.schlaubi.tonbrett.app.api.AppContext
import dev.schlaubi.tonbrett.app.api.getUrl
import dev.schlaubi.tonbrett.app.api.tokenKey
import dev.schlaubi.tonbrett.client.href
import dev.schlaubi.tonbrett.common.Route
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

    @Composable
    private fun AuthorizationScreen() {
        LaunchedEffect(Unit) { launchAuthorization() }

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
