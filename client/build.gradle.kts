import dev.schlaubi.tonbrett.gradle.androidSdk
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    `multiplatform-module`
    `published-module`
}

repositories {
    mavenCentral()
    google()
}

kotlin {
    macosArm64()
    macosX64()
    mingwX64()

    sourceSets {
        commonMain {
            dependencies {
                api(projects.common)
                api(libs.kotlin.logging)
                api(libs.ktor.client.core)
                api(libs.ktor.client.resources)
                api(libs.ktor.client.content.negotiation)
                api(libs.ktor.client.auth)
                api(libs.ktor.serialization.kotlinx.json)
            }
        }

        jsMain {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }

        appleMain {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        mingwMain {
            dependencies {
                implementation(libs.ktor.client.winhttp)
            }
        }
    }
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xdont-warn-on-error-suppression")
    }
}

android {
    namespace = "dev.schlaubi.tonbrett.client"
    compileSdkVersion = androidSdk
}
