import dev.schlaubi.tonbrett.gradle.androidSdk
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    `published-module`
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
                withAndroidTarget()
                withJvm()
            }
        }
    }
    jvm()
    js(IR) {
        browser()
    }
    androidTarget()
    iosSimulatorArm64()
    iosX64()
    iosArm64()
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

        named("mingwMain") {
            dependencies {
                implementation(libs.ktor.client.winhttp)
            }
        }
    }
}

android {
    namespace = "dev.schlaubi.tonbrett.client"
    compileSdkVersion = androidSdk
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }
}
