@file:JsModule("@discord/embedded-app-sdk")

package dev.schlaubi.tonbrett.app.web

import kotlin.js.Promise

external interface AuthorizeInput : JsAny {
    @JsName("client_id")
    val clientId: JsString
    val scope: JsArray<JsString>

    @JsName("response_type")
    val responseType: JsString

    @JsName("code_challenge")
    val codeChallenge: JsString?
    val state: JsString?
    val prompt: JsString?

    @JsName("code_challenge_method")
    val codeChallengeMethod: JsString?
}

external class AuthorizeResponse : JsAny {
    val code: JsString
}

external class DiscordSDK constructor(oauthClientId: String) : JsAny {
    val commands: Commands
    fun ready(): Promise<JsAny?>

    class Commands {
        fun authorize(input: AuthorizeInput): Promise<AuthorizeResponse>
    }
}
