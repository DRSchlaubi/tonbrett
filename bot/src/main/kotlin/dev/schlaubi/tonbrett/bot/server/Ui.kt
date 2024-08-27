package dev.schlaubi.tonbrett.bot.server

import dev.schlaubi.tonbrett.common.Route.AuthDeepLink
import dev.schlaubi.tonbrett.common.Route.Ui
import io.ktor.http.HttpHeaders
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.resources.*
import io.ktor.server.response.header
import io.ktor.server.routing.*
import kotlin.reflect.jvm.javaField

private inline fun <reified T : Any> Route.staticUi(index: String? = "index.html") = resource<T> {
    intercept(ApplicationCallPipeline.Plugins) {
        call.fixClassLoader()
    }
    staticResources("", "web", index = index)
}

fun Route.ui() {
    staticUi<Ui.Login>()
    staticUi<Ui>()
    staticUi<AuthDeepLink>()
    staticUi<Ui.DiscordActivity>(index = "discord-activity.html")
}

private fun ApplicationCall.fixClassLoader() {
    val fixedApplication = Application(HackedEnvironment(application.environment))
    val call = (this as RoutingApplicationCall).engineCall
    BaseApplicationCall::application.javaField!!.apply {
        isAccessible = true
        set(call, fixedApplication)
    }
}

private class HackedEnvironment(val parent: ApplicationEnvironment) : ApplicationEnvironment by parent {
    override val classLoader: ClassLoader
        get() = javaClass.classLoader
}
