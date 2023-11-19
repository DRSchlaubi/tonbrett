import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension

plugins {
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose) apply false
}

allprojects {
    group = "dev.schlaubi.tonbrett"
    version = "1.16.22"

    repositories {
        mavenCentral()
        google()
        maven("https://jitpack.io")
    }
}

subprojects {
    afterEvaluate {
        extensions.findByType<KotlinTopLevelExtension>()?.apply {
            jvmToolchain(21)
        }
    }
}
