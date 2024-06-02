package dev.schlaubi.tonbrett.app.desktop

import dev.schlaubi.tonbrett.app.api.AppContext
import dev.schlaubi.tonbrett.app.desktop.Platform.getToken
import dev.schlaubi.tonbrett.app.desktop.Platform.setToken


abstract class TokenStorageAppContext : AppContext() {
    override var token: String
        get() = getToken()
        set(value) = setToken(value)
}
