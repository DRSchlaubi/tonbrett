 plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.library) apply false
     id("org.jetbrains.kotlin.android") version "1.8.20" apply false
 }

allprojects {
    group = "dev.schlaubi.tonbrett"
    version = "1.7.0"

    repositories {
        mavenCentral()
    }
}
