plugins {
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose) apply false
}

allprojects {
    group = "dev.schlaubi.tonbrett"
    version = "3.3.3"

    repositories {
        mavenCentral()
        google()
        maven("https://releases-repo.kordex.dev")
        maven("https://snapshots-repo.kordex.dev")
        maven("https://mirror-repo.kordex.dev")
    }
}
