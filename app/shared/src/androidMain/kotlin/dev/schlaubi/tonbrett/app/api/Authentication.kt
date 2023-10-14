package dev.schlaubi.tonbrett.app.api

import android.app.Activity
import com.liftric.kvault.KVault

actual open class AppContext(val currentActivity: Activity) : AppContextBase() {
     override val vault: KVault = KVault(currentActivity)

     actual open fun reAuthorize() = onReAuthorize()
 }
