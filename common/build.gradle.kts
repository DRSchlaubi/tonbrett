@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import dev.schlaubi.tonbrett.gradle.androidSdk
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    `multiplatform-module`
    kotlin("plugin.serialization")
    `published-module`
}

kotlin {
    targets.named<KotlinJvmTarget>("desktop") {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }
    explicitApi()

    macosArm64()
    macosX64()
    mingwX64()

    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.serialization)
                compileOnly(libs.ktor.resources)
            }
        }

        named("jvmMain") {
            dependencies {
                compileOnly(libs.kmongo.id.serialization)
                compileOnly(libs.kord.common)
                compileOnly(libs.kmongo.serialization)
            }
        }

        androidMain {
            dependencies {
                api(libs.kmongo.id.serialization)
                api(libs.kord.common)
                api(libs.kmongo.serialization)
            }
        }
    }
}

android {
    namespace = "dev.schlaubi.tonbrett.common"
    compileSdkVersion = androidSdk
}
