import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension

plugins {
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose) apply false
}

allprojects {
    group = "dev.schlaubi.tonbrett"
    version = "1.20.1"

    repositories {
        mavenCentral()
        google()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

subprojects {
    afterEvaluate {
        extensions.findByType<KotlinTopLevelExtension>()?.apply {
            jvmToolchain(22)
        }
    }
}
