package dev.schlaubi.tonbrett.app.desktop

import dev.schlaubi.tonbrett.app.api.AppContext

abstract class ConfigBasedAppContext : AppContext() {
    override var token: String
        get() = getConfig().sessionToken ?: error("Please sign in")
        set(value) = saveConfig(Config(sessionToken = value))
}
