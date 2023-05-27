package dev.schlaubi.tonbrett.app.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import dev.schlaubi.tonbrett.app.api.AppContext
import dev.schlaubi.tonbrett.app.api.getUrl
import dev.schlaubi.tonbrett.app.api.tokenKey
import dev.schlaubi.tonbrett.common.Route
import dev.schlaubi.tonbrett.client.href
import io.ktor.http.URLBuilder

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.btn_retry)
        button.setOnClickListener {
            onAttachedToWindow()
        }
    }

    override fun onAttachedToWindow() {
        val context = AppContext(applicationContext)
        if (intent.data?.scheme == "tonbrett" && intent.data?.host == "login") {
            val token = intent.data?.getQueryParameter("token")
            if (token != null) {
                context.vault.set(tokenKey, token)
                startActivity(Intent(this, AppActivity::class.java))
                return
            }
        }
        if (context.getTokenOrNull() == null) {
            val urlBuilder = URLBuilder(getUrl())
            href(Route.Auth(Route.Auth.Type.MOBILE_APP), urlBuilder)
            val intent = CustomTabsIntent.Builder()
                .build()
            intent.launchUrl(this, Uri.parse(urlBuilder.buildString()))
        } else {
            startActivity(Intent(this, AppActivity::class.java))
        }
    }
}
