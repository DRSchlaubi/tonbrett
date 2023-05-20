 plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

allprojects {
    group = "dev.schlaubi.tonbrett"
    version = "1.2.2"

    repositories {
        mavenCentral()
    }
}
