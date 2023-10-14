package dev.schlaubi.tonbrett.app.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import dev.schlaubi.tonbrett.app.MobileTonbrettApp
import dev.schlaubi.tonbrett.app.ProvideImageLoader
import dev.schlaubi.tonbrett.app.api.AppContext
import dev.schlaubi.tonbrett.app.api.ProvideContext
import dev.schlaubi.tonbrett.app.api.tokenKey

class AppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = if (intent.data?.scheme == "tonbrett" && intent.data?.host == "login") {
            intent.data?.getQueryParameter("token")
        } else {
            null
        }
        setContent {
            val context = object : AppContext(this) {
                override fun reAuthorize() {
                    vault.deleteObject(tokenKey)
                    super.reAuthorize()
                }
            }

            ProvideContext(context) {
                ProvideImageLoader {
                    UpdateAwareAppScope(activity = this) {
                        MobileTonbrettApp(token)
                    }
                }
            }
        }
    }
}
