import dev.schlaubi.tonbrett.gradle.androidSdk
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

repositories {
    mavenCentral()
    google()
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    targetHierarchy.default {
        common {
            group("jvmShared") {
                withAndroid()
                withJvm()
            }
        }
    }
    jvm()
    js(IR) {
        browser()
    }
    android {
        compilations.all {
            compilerOptions.options.jvmTarget = JvmTarget.JVM_1_8
        }
    }
    iosSimulatorArm64()
    iosX64()
    iosArm64()

    sourceSets {
        commonMain {
            dependencies {
                api(projects.common)
                api(libs.kotlin.logging)
                api(libs.ktor.client.core)
                api(libs.ktor.client.resources)
                api(libs.ktor.client.content.negotiation)
                api(libs.ktor.serialization.kotlinx.json)
            }
        }

        named("jsMain") {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }

        named("appleMain") {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        named("jvmSharedMain") {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }
    }
}

android {
    namespace = "dev.schlaubi.tonbrett.client"
    compileSdkVersion = androidSdk
}
