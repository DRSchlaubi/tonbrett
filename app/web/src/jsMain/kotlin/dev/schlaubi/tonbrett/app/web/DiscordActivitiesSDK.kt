@file:JsModule("@discord/embedded-app-sdk")
@file:JsNonModule
package dev.schlaubi.tonbrett.app.web

import kotlin.js.Promise

external interface AuthorizeInput {
    @JsName("client_id")
    val clientId: String
    val scope: Array<String>
    @JsName("response_type")
    val responseType: String
    @JsName("code_challenge")
    val codeChallenge: String?
    val state: String?
    val prompt: String?
    @JsName("code_challenge_method")
    val codeChallengeMethod: String?
}

external class AuthorizeResponse {
    val code: String
}

external class DiscordSDK constructor(oauthClientId: String) {
    val commands: Commands
    fun ready(): Promise<Unit>

    class Commands {
        fun authorize(input: AuthorizeInput): Promise<AuthorizeResponse>
    }
}
