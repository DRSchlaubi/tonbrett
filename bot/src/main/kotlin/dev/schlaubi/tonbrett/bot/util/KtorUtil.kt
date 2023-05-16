package dev.schlaubi.tonbrett.bot.util

import io.ktor.server.plugins.*

fun notFound(message: String): Nothing = throw NotFoundException(message)
fun soundNotFound(): Nothing = notFound("Sound not found")
