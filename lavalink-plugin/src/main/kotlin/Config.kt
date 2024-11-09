package dev.schlaubi.tonbrett.lavalink

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("plugins.tonbrett")
@Component
class Config {
    var token: String? = null
    var host: String? = null
}
