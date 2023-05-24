package dev.schlaubi.tonbrett.common

import io.ktor.resources.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * /soundboard - all plugin routes
 */
@Resource("/soundboard")
public class Route {

    /**
     * /soundsboard/tags - Lists all available tags
     *
     * @property query an optional query the tags should match
     * @property limit an optional limit of how many tags to receive at most
     */
    @Resource("tags")
    public data class Tags(val query: String? = null, val limit: Int? = null, val parent: Route = Route())

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
        val parent: Route = Route()
    ) {
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
    public data class Auth(val type: Type? = null, val parent: Route = Route()) {
        /**
         * /soundboard/auth/callback - oauth callback
         */
        @Resource("callback")
        public data class Callback(val parent: Auth = Auth())

        @Serializable
        public enum class Type(public val redirectTo: String) {
            WEB("/soundboard/ui/login?token="),
            APP("http://localhost:12548/login?token="),
            MOBILE_APP("tonbrett://login?token=")
        }
    }

    @Resource("users/@me")
    public data class Me(val parent: Route = Route()) {
        @Resource("events")
        public data class Events(@SerialName("session_token") val sessionToken: String, val parent: Me = Me())
    }

    @Resource("ui")
    public data class Ui(val parent: Route = Route()) {
        @Resource("login")
        public data class Login(val token: String, val parent: Ui = Ui())
    }
}
