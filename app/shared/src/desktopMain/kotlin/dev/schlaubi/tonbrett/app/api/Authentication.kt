package dev.schlaubi.tonbrett.app.api

actual abstract class AppContextBase : ApiStateHolder() {
    actual open val isSignedIn: Boolean
        get() = TODO("Please implement sign in")
}

actual open class AppContext : AppContextBase() {
    override var token: String
        get() = TODO("Please implement this in inheritor")
        set(_) = TODO("Please implement this in inheritor")

    actual open fun reAuthorize() = Unit
}
