package dev.schlaubi.tonbrett.app.web

import dev.schlaubi.tonbrett.app.TonbrettApp
import dev.schlaubi.tonbrett.app.api.AppContext
import dev.schlaubi.tonbrett.app.api.ProvideContext
import dev.schlaubi.tonbrett.app.api.tokenKey
import dev.schlaubi.tonbrett.app.web.resource.ComposeRouteHandler
import dev.schlaubi.tonbrett.app.web.resource.routeHandler
import dev.schlaubi.tonbrett.client.href
import dev.schlaubi.tonbrett.common.Route
import kotlinx.browser.sessionStorage
import kotlinx.browser.window
import org.w3c.dom.get

private val context = AppContext()

fun main() {
    routeHandler<Route.Ui.Login> { (token) ->
        context.token = token
        window.location.href = href(Route.Ui())
    }
    ComposeRouteHandler<Route.AuthDeepLink> { (protocol, cli) ->
        AuthorizationScreen(cli, protocol)
    }
    ComposeRouteHandler<Route.Ui> {
        if (sessionStorage[tokenKey] == null) {
            window.location.href = href(Route.Auth(type = Route.Auth.Type.WEB))
        } else {
            ProvideContext(context) {
                TonbrettApp()
            }
        }
    }
}
