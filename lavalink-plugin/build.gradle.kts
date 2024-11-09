plugins {
    kotlin("jvm")
    alias(libs.plugins.lavalink)
    `published-module`
}

dependencies {
    implementation(projects.client)
    implementation(libs.kmongo.id.serialization)
    implementation(libs.kord.common)
}

lavalinkPlugin {
    name = "tonbrett"
    apiVersion = libs.versions.lavalink.api
    path = "dev.schlaubi.tonbrett.lavalink"
    configurePublishing = true
}
