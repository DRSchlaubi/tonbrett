package dev.schlaubi.tonbrett.app.api

actual open class AppContext : AppContextBase() {
    actual override val isSignedIn: Boolean
        get() = TODO("Not yet implemented")
    actual override var token: String
        get() = TODO("Please implement this in inheritor")
        set(_) = TODO("Please implement this in inheritor")

    actual open fun reAuthorize() = Unit
}
