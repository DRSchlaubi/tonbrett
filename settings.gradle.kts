rootProject.name = "tonbrett"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":common", ":bot", ":client")

include(
    ":app",
    ":app:shared",
    ":app:web",
    ":app:desktop",
    ":app:android"
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    }
}
