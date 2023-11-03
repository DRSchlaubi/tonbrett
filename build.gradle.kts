import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension

plugins {
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose) apply false
}

allprojects {
    group = "dev.schlaubi.tonbrett"
    version = "1.16.10"

    repositories {
        mavenCentral()
        google()
    }
}

subprojects {
    afterEvaluate {
        extensions.findByType<KotlinTopLevelExtension>()?.apply {
            jvmToolchain(21)
        }
    }
}
