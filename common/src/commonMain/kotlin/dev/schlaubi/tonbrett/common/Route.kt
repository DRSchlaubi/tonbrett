package dev.schlaubi.tonbrett.common

import io.ktor.resources.*
import kotlinx.serialization.SerialName

/**
 * /soundboard - all plugin routes
 */
@Resource("/soundboard")
public class Route {
    /**
     * /soundboard/sounds - endpoint for listing/modifying all sounds.
     *
     * @property onlyMine only list my sounds (only for GET)
     * @property query filter sounds for a query (only for GET)
     */
    @Resource("sounds")
    public data class Sounds(
        @SerialName("only_mine")
        val onlyMine: Boolean = false,
        val query: String? = null,
        val parent: Route = Route()) {
        /**
         * /soundsboard/sounds/[id] - Endpoint for modifying/retrieving a specific sound.
         */
        @Resource("{id}")
        public data class Sound(val id: String, val parent: Sounds = Sounds()) {

            /**
             * /soundboard/sound/[id][Sound.id]/audio - Retrieves the binary audio data
             * for this sound
             */
            @Resource("audio")
            public class Audio(public val parent: Sound) {
                public constructor(id: String) : this(Sound(id))

                public operator fun component1(): String = parent.id
            }

            @Resource("play")
            public class Play(public val parent: Sound) {
                public constructor(id: String) : this(Sound(id))

                public operator fun component1(): String = parent.id
            }
        }
    }

    /**
     * /soundboard/auth - initiate Discord OAuth authorization
     */
    @Resource("auth")
    public data class Auth(val parent: Route = Route()) {
        /**
         * /soundboard/auth/callback - oauth callback
         */
        @Resource("callback")
        public data class Callback(val parent: Auth = Auth())
    }

    @Resource("users/@me")
    public data class Me(val parent: Route = Route()) {
        @Resource("events")
        public data class Events(val parent: Me = Me())
    }
}
