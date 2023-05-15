package dev.schlaubi.tonbrett.common

import io.ktor.resources.*

@Resource("/soundboard")
public class Route {
    @Resource("audio/{audioId}")
    public data class Audio(val audioId: String, val parent: Route = Route())
}
