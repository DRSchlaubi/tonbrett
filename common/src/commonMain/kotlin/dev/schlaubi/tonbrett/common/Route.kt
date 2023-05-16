package dev.schlaubi.tonbrett.common

import io.ktor.resources.*

@Resource("/soundboard")
public class Route {
    @Resource("sounds")
    public data class Sounds(val parent: Route = Route()) {
        @Resource("{id}")
        public data class Sound(val id: String, val parent: Sounds = Sounds()) {
            @Resource("audio")
            public class Audio(public val parent: Sound) {
                public constructor(id: String) : this(Sound(id))

                public operator fun component1(): String = parent.id
            }
        }
    }
}
