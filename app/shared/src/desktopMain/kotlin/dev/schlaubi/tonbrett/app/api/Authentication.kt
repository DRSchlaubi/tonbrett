package dev.schlaubi.tonbrett.app.api


actual open class AppContext : AppContextBase() {
    override var token: String
        get() = TODO("Please implement this in inheritor")
        set(_) = TODO("Please implement this in inheritor")

    actual open fun reAuthorize() = Unit
}
