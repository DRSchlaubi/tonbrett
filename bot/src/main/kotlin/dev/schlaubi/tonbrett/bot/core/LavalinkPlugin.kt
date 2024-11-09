@file:OptIn(PluginApi::class)
@file:Suppress("INVISIBLE_REFERENCE")

package dev.schlaubi.tonbrett.bot.core

import dev.kordex.core.ExtensibleBot
import dev.schlaubi.lavakord.MutableLavaKordOptions
import dev.schlaubi.lavakord.Plugin
import dev.schlaubi.lavakord.PluginApi
import dev.schlaubi.lavakord.audio.Node
import dev.schlaubi.lavakord.audio.RestNode
import dev.schlaubi.lavakord.rest.restClient
import dev.schlaubi.mikmusic.core.audio.LavalinkExtensionPoint
import dev.schlaubi.mikmusic.core.audio.LavalinkManager
import dev.schlaubi.tonbrett.common.Sound
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.URLProtocol
import io.ktor.http.appendPathSegments
import io.ktor.http.isSecure
import io.ktor.http.takeFrom
import io.ktor.resources.Resource
import org.pf4j.Extension

@Resource("v4/sounds")
class Route {
    @Resource("/sync")
    data class Sync(val parent: Route = Route())

    @Resource("{id}")
    data class Sound(val id: String, val parent: Route = Route())
}

object Tonbrett : Plugin {
    override val name: String = "tonbrett"
    override val version: String = "2.0.1"

}

suspend fun Node.syncSounds() = request(HttpMethod.Post, "v4", "sounds", "sync")
suspend fun Node.deleteSound(id: String) = request(HttpMethod.Delete, "v4", "sounds", id)
suspend fun Node.syncSound(sound: Sound) = request(HttpMethod.Put, "v4", "sounds") {
    setBody(sound)
}

suspend fun ExtensibleBot.syncSound(sound: Sound) = findExtension<LavalinkManager>()!!.lavalink.nodes.forEach {
    it.syncSound(sound)
}

suspend fun ExtensibleBot.deleteSound(id: String) = findExtension<LavalinkManager>()!!.lavalink.nodes.forEach {
    it.deleteSound(id)
}

// We cannot use resources, because the music plugin and the ktor plugin have separate versions of it
// The only way to fix this is to introduce a common plugin adding that shared between the two
// which is not worth it
@PluginApi
private suspend fun RestNode.request(
    method: HttpMethod,
    vararg url: String,
    builder: HttpRequestBuilder.() -> Unit = {}
) {
    val nodeHost = host
    return restClient.request {
        this.method = method
        url {
            takeFrom(nodeHost)
            appendPathSegments(*url)
            // URL is prefix with WebSocket protocol
            protocol = if (protocol.isSecure()) URLProtocol.HTTPS else URLProtocol.HTTP
        }

        header(HttpHeaders.Authorization, authenticationHeader)
        accept(ContentType.Application.Json)
        builder()
    }.body()
}

@Extension
class TonbrettLavalinkExtension : LavalinkExtensionPoint {
    override fun MutableLavaKordOptions.apply() {
        plugins {
            install(Tonbrett)
        }
    }
}
