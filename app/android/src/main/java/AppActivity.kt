package dev.schlaubi.tonbrett.app.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import dev.schlaubi.tonbrett.app.ProvideImageLoader
import dev.schlaubi.tonbrett.app.TonbrettApp
import dev.schlaubi.tonbrett.app.api.AppContext
import dev.schlaubi.tonbrett.app.api.ProvideContext
import dev.schlaubi.tonbrett.app.api.tokenKey
import kotlin.system.exitProcess

class AppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appUpdateManager = AppUpdateManagerFactory.create(applicationContext)

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                startActivity(Intent(applicationContext, UpdateActivity::class.java))
                finish()
            }
        }


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
