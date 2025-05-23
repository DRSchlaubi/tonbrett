import com.github.gmazzo.buildconfig.generators.BuildConfigKotlinGenerator
import dev.schlaubi.tonbrett.gradle.androidSdk
import dev.schlaubi.tonbrett.gradle.apiUrl
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    `multiplatform-module`
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.compose")
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.ksp)
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    applyDefaultHierarchyTemplate {
        common {
            group("nonWeb") {
                withNative()
                withAndroidTarget()
                withJvm()
            }

            group("skia") {
                withApple()
                withJvm()
                withJs()
                withWasmJs()
            }

            group("mobile") {
                group("apple") {
                    withApple()
                }
                withAndroidTarget()

            }
            group("nonDesktop") {
                withJs()
                withApple()
                withAndroidTarget()
                withWasmJs()
            }
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
        }
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
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
                implementation(compose.materialIconsExtended)

                implementation(libs.coil.compose)
                implementation(libs.coil.network)
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
            }
        }

        named("desktopMain") {
            dependencies {
                implementation(libs.kmongo.id.serialization)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }

        named("mobileMain") {
            dependencies {
                api(libs.kvault)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.browser)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.coil.gif)
                implementation(libs.coil.svg)
            }
        }
    }
}

dependencies {
    // This is buggy * ios 18
//    kspCommonMainMetadata(libs.lyricist.processor)
    "desktopMainImplementation"(libs.kord.common) {
        exclude(group = "io.ktor")
    }
}

buildConfig {
    packageName("dev.schlaubi.tonbrett.app.shared")
    buildConfigField("String", "API_URL", "\"${project.apiUrl}\"")

    sourceSets {
        all {
            generator = BuildConfigKotlinGenerator()
        }
    }
}

android {
    namespace = "dev.schlaubi.tonbrett.app"
    compileSdkVersion = androidSdk

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
    }
}

