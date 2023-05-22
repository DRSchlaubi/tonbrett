package dev.schlaubi.tonbrett.app.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
 import dev.schlaubi.tonbrett.app.ProvideImageLoader
import dev.schlaubi.tonbrett.app.TonbrettApp
import dev.schlaubi.tonbrett.app.api.AppContext
import dev.schlaubi.tonbrett.app.api.ProvideContext
import dev.schlaubi.tonbrett.app.api.tokenKey

class AppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = object : AppContext(applicationContext) {
                override fun reAuthorize() {
                    vault.deleteObject(tokenKey)
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                }
            }
            context.resetApi()
            ProvideContext(context) {
                ProvideImageLoader {
                    TonbrettApp()
                }
            }
        }
    }
}
