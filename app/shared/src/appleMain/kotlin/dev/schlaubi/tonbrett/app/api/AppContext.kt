package dev.schlaubi.tonbrett.app.api

import com.liftric.kvault.KVault
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.UIKit.UIViewController

actual val Dispatchers.IO: CoroutineDispatcher
    get() = IO

actual open class AppContext : AppContextBase() {
    override val vault: KVault = KVault()
    open fun present(viewController: UIViewController): Unit = TODO("Please implement this")
    actual open fun reAuthorize() = onReAuthorize()
}