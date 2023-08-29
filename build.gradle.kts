import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose) apply false
}

allprojects {
    group = "dev.schlaubi.tonbrett"
    version = "1.12.15"

    repositories {
        mavenCentral()
    }
}

subprojects {
    if (name != "app") {
        afterEvaluate {
            configure<KotlinTopLevelExtension> {
                jvmToolchain(20)
            }
        }
    }
}
