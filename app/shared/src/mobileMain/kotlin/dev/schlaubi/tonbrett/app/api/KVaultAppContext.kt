package dev.schlaubi.tonbrett.app.api


const val tokenKey = "token"

interface MobileAppContext {
    var token: String
    var onReAuthorize: () -> Unit

    fun withReAuthroize(block: () -> Unit) {
        onReAuthorize = block
    }
}
