import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import dev.schlaubi.tonbrett.gradle.apiUrl
import dev.schlaubi.tonbrett.gradle.androidSdk
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.ksp)
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    targetHierarchy.default {
        common {
            group("nonWeb") {
                withNative()
                withAndroid()
                withJvm()

                group("nonWebSkia") {
                    withApple()
                    withJvm()
                }
            }

            group("jvm") {
                withAndroid()
                withJvm()
            }

            group("mobile") {
                withApple()
                withAndroid()
            }
        }
    }
    android {
        compilations.all {
            compilerOptions.options.jvmTarget = JvmTarget.JVM_1_8
        }
    }
    jvm("desktop")
    js(IR) {
        browser()
    }
    iosSimulatorArm64()
    iosX64()
    iosArm64()

    sourceSets {
        all {
            languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
        }
        commonMain {
            kotlin.srcDir(file("$buildDir/generated/ksp/metadata/commonMain/kotlin"))
            kotlin.srcDir(file("$buildDir/generated/buildConfig/metadata/main"))
            dependencies {
                api(projects.client)
                api(libs.lyricist)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.material3)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
            }
        }

        named("jsMain") {
            dependencies {
                api("org.jetbrains.kotlin:kotlinx-atomicfu-runtime:1.8.20")
            }
        }

        named("jvmMain") {
            dependencies {
                implementation(libs.kmongo.id.serialization)
                implementation(libs.kord.common)
                implementation(libs.imageloader)
            }
        }

        named("mobileMain") {
            dependencies {
                api(libs.kvault)
            }
        }
    }
}

buildConfig {
    packageName("dev.schlaubi.tonbrett.app.shared")
    buildConfigField("String", "API_URL", "\"${project.apiUrl}\"")
}

android {
    namespace = "dev.schlaubi.tonbrett.app"
    compileSdkVersion = androidSdk

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
    }
}
