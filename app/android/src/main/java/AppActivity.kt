package dev.schlaubi.tonbrett.app.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import dev.schlaubi.tonbrett.app.MobileTonbrettApp
import dev.schlaubi.tonbrett.app.api.AppContext
import dev.schlaubi.tonbrett.app.api.ProvideContext

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val token = if (intent.data?.scheme == "tonbrett" && intent.data?.host == "login") {
            intent.data?.getQueryParameter("token")
        } else {
            null
        }

        setContent {
            val context = AppContext(this)

            //Disable for now
            //WearOSTokenSharing(token = token ?: context.getTokenOrNull())

            ProvideContext(context) {
                UpdateAwareAppScope(activity = this) {
                    MobileTonbrettApp(token) { url ->
                        val intent = CustomTabsIntent.Builder().build()
                        intent.launchUrl(this@AppActivity, url.toUri())
                    }
                }
            }
        }
    }
}
