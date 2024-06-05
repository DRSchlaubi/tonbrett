package dev.schlaubi.tonbrett.app.ios

import androidx.compose.ui.window.ComposeUIViewController
import dev.schlaubi.tonbrett.app.MobileTonbrettApp
import dev.schlaubi.tonbrett.app.api.ProvideContext
import platform.UIKit.UIViewController

@Suppress("FunctionName", "unused")
fun MainUiViewController(receivedToken: String?, onAuth: (url: String) -> Unit): UIViewController {
    var onPresent: (UIViewController) -> Unit = {}
    val context = object : AppleAppContext() {
        override fun present(viewController: UIViewController) = onPresent(viewController)

    }
    val controller = ComposeUIViewController {
        ProvideContext(context) {
            MobileTonbrettApp(receivedToken, onAuth)
        }
    }

    onPresent = {
        controller.presentViewController(it, true, null)
    }

    return controller
}
