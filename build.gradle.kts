plugins {
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose) apply false
}

allprojects {
    group = "dev.schlaubi.tonbrett"
    version = "2.3.12"

    repositories {
        mavenCentral()
        google()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }
}
