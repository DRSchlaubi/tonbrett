plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
}

allprojects {
    group = "dev.schlaubi.tonbrett"
    version = "1.9.0"

    repositories {
        mavenCentral()
    }
}
