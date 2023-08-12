package dev.schlaubi.tonbrett.app.api


actual open class AppContext : AppContextBase() {
    override var token: String
        get() = getConfig().sessionToken ?: error("Please sign in")
        set(value) { saveConfig(Config(sessionToken = value)) }

    actual open fun reAuthorize() = Unit
}
