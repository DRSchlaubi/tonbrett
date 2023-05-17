 plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

allprojects {
    group = "dev.schlaubi"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}
