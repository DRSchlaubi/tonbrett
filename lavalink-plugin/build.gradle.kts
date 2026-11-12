import dev.schlaubi.tonbrett.gradle.minimalJvmTarget

plugins {
    kotlin("jvm")
    alias(libs.plugins.lavalink)
    `published-module`
}

repositories {
    maven("https://europe-west3-maven.pkg.dev/mik-music/mikbot")
}

dependencies {
    // Lavalink cannot use modern kx.ser, so we have to downgrade to older versions
    implementation("dev.schlaubi.tonbrett:client:2.1.3")
    implementation("dev.kord:kord-common:0.15.0")
    implementation("org.litote.kmongo:kmongo-id-serialization:5.1.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = minimalJvmTarget
    }
}

lavalinkPlugin {
    name = "tonbrett"
    apiVersion = libs.versions.lavalink.api
    path = "dev.schlaubi.tonbrett.lavalink"
    configurePublishing = true
}
