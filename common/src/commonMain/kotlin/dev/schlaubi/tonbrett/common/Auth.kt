package dev.schlaubi.tonbrett.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class AuthRefreshRequest(val expiredJwt: String)

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
