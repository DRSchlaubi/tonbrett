import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

lavalinkPlugin {
    name = "tonbrett"
    apiVersion = libs.versions.lavalink.api
    path = "dev.schlaubi.tonbrett.lavalink"
    configurePublishing = true
}
