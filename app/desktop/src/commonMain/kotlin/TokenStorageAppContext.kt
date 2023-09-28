package dev.schlaubi.tonbrett.app.desktop

import dev.schlaubi.tonbrett.app.api.AppContext

abstract class TokenStorageAppContext : AppContext() {
    override var token: String
        get() = getToken()
        set(value) = setToken(value)
}
