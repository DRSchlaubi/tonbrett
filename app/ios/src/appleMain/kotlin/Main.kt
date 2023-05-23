import androidx.compose.ui.window.ComposeUIViewController
import dev.schlaubi.tonbrett.app.TonbrettApp
import dev.schlaubi.tonbrett.app.api.ProvideContext
import platform.UIKit.UIAlertController
import platform.UIKit.UIViewController

@Suppress("FunctionName", "unused")
fun MainUiViewController(): UIViewController {
    var onReauthorization = {}
    val context = object : AppleAppContext() {
        init {
            resetApi()
        }

        override fun reAuthorize() {
            onReauthorization()
        }
    }
    val controller = ComposeUIViewController {
        ProvideContext(context) {
            TonbrettApp()
        }
    }

    onReauthorization = {
        val alert = UIAlertController("restart", null).apply {
            title = "Please restart the App"
        }
        controller.presentViewController(alert, false, null)
    }

    return controller
}
