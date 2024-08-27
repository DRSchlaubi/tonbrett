package dev.schlaubi.tonbrett.common

import io.ktor.http.*
import io.ktor.resources.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import dev.schlaubi.tonbrett.common.Sound as SoundEntity

public const val authServerPort: Int = 12548

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
     * /soundboard/sounds - Parent for sound endpoints.
     */
    @Resource("sounds")
    public data class Sounds(val parent: Route = Route()) {
        /**
         * /soundboard/sounds - endpoint for listing/modifying all sounds.
         *
         * @property onlyMine only list my sounds (only for GET)
         * @property query filter sounds for a query (only for GET)
         * @property useUnicode whether to use [SoundEntity.DiscordEmoji] or [SoundEntity.Twemoji]
         */
        @Resource("sounds")
        public data class ListSounds(
            @SerialName("only_mine")
            val onlyMine: Boolean = false,
            val query: String? = null,
            @SerialName("use_unicode")
            val useUnicode: Boolean = false,
            val parent: Route = Route()
        )

        /**
         * /soundsboard/sounds/[id] - Endpoint for modifying/retrieving a specific sound.
         */
        @Resource("{id}")
        public data class Sound(val id: String, val parent: Sounds = Sounds()) {

            /**
             * /soundboard/sound/[id][Sound.id]/audio - Retrieves the binary audio data
             * for this sound
             *
             * @property contentType the content type you want to request the audio in
             */
            @Resource("audio")
            public class Audio(public val parent: Sound, public val contentType: String? = null) {
                public constructor(id: String, contentType: ContentType? = null) : this(
                    Sound(id),
                    contentType?.toString()
                )

                public operator fun component1(): String = parent.id
                public operator fun component2(): String? = contentType
            }

            /**
             * /soundboard/sound/[id][Sound.id]/audio - Plays this sound.
             */
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

        /**
         * /soundboard/auth/token - requests a token for this auth code
         */
        @Resource("token")
        public data class Token(val code: String, val state: String, val parent: Auth = Auth())

        /**
         * /soundboard/auth/refresh - refresh JWTs
         */
        @Resource("refresh")
        public data class Refresh(val parent: Auth = Auth())

        /**
         * Types of authentication flows.
         */
        @Serializable
        public enum class Type(public val redirectTo: String) {
            /**
             * Web app authentication flow.
             */
            WEB("/soundboard/ui/login?token="),

            /**
             * Desktop app via localhost callback server.
             */
            APP("/soundboard/deeplink/login?token="),

            /**
             * Desktop app via `tonbrett://` protocol (used on UWP).
             */
            PROTOCOL("/soundboard/deeplink/login?protocol=true&token="),

            /**
             * CLI login page displaying login command.
             */
            CLI("/soundboard/deeplink/login?cli=true&token="),

            /**
             * Mobile app via protocol for in app browser.
             */
            MOBILE_APP("tonbrett://login?token=")
        }
    }

    /**
     * /soundboard/users/@me - current user status.
     */
    @Resource("users/@me")
    public data class Me(val parent: Route = Route()) {
        /**
         * /soundboard/users/@me/events - websocket entrypoint for live updates
         *
         * @property sessionToken the session token
         * @property useUnicode whether to use [SoundEntity.DiscordEmoji] or [SoundEntity.Twemoji]
         */
        @Resource("events")
        public data class Events(
            @SerialName("session_token") val sessionToken: String,
            @SerialName("use_unicode")
            val useUnicode: Boolean = false,
            val parent: Me = Me()
        )
    }

    /**
     * /soundboard/ui - WebUI base path
     */
    @Resource("ui")
    public data class Ui(val parent: Route = Route()) {
        /**
         * /soundboard/ui/login - Path to save current token
         *
         * @property token the token to save
         */
        @Resource("login")
        public data class Login(val token: String, val parent: Ui = Ui())

        @Resource("discord-activity")
        public data class DiscordActivity(val parent: Ui = Ui())
    }

    /**
     * /soundboard/player/stop - stops playing current track
     */
    @Resource("player/stop")
    public data class StopPlayer(val parent: Route = Route())

    /**
     * /soundboard/deeplink/login - Deeplink into the app for authorization.
     *
     * @property protocol whether to use the `tonbrett://` protocol to log in
     * @property cli whether to show the CLI login command
     */
    @Resource("deeplink/login")
    public data class AuthDeepLink(
        val protocol: Boolean = false,
        val cli: Boolean = false,
        val parent: Route = Route()
    )
}
