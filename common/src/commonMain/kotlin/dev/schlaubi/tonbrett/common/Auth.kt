package dev.schlaubi.tonbrett.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request for a new authentication token.
 *
 * @property expiredJwt the old expired JWT (needs to contain a Discord refresh token)
 */
@Serializable
public data class AuthRefreshRequest(val expiredJwt: String)

/**
 * Response to an authentication refresh request.
 *
 * @property newJwt the new JWT
 */
@Serializable
public data class AuthRefreshResponse(val newJwt: String)

// https://discord.com/developers/docs/topics/oauth2#authorization-code-grant-access-token-response
@Serializable
public data class DiscordAccessTokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("expires_in")
    val expiresIn: Long,
    @SerialName("refresh_token")
    val refreshToken: String,
)
