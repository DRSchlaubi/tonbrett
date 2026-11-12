@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import dev.schlaubi.tonbrett.gradle.minimalJvmTarget
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    `multiplatform-module`
    kotlin("plugin.serialization")
    `published-module`
}

kotlin {
    targets.named<KotlinJvmTarget>("desktop") {
        compilerOptions {
            jvmTarget = minimalJvmTarget
        }
    }
    explicitApi()

    macosArm64()
    mingwX64()

    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.serialization)
                compileOnly(libs.ktor.resources)
            }
        }

        javaMain {
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
