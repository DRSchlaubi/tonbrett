package dev.schlaubi.tonbrett.app.web

import kotlin.js.Promise

external fun authorize(discordSdk: JsAny, appId: JsString, state: JsString): Promise<AuthorizeResponse>

fun DiscordSDK.authorize(appId: String, state: String) =
    authorize(this, appId.toJsString(), state.toJsString())
