pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.6.0"
}

rootProject.name = "tonbrett"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":common", ":bot", ":client")

include(
    ":app",
    ":app:shared",
    ":app:web",
    ":app:desktop",
    ":app:android",
    ":app:ios"
)

