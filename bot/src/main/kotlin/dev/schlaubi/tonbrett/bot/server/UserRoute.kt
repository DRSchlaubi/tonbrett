package dev.schlaubi.tonbrett.bot.server

import dev.schlaubi.tonbrett.bot.core.voiceState
import dev.schlaubi.tonbrett.common.Route.Me
import dev.schlaubi.tonbrett.common.User
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.users() {
    get<Me> {
        val user = call.userId
        val voiceState = user.voiceState

        call.respond(User(
            user,
            voiceState
        ))
    }
}
