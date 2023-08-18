import dev.schlaubi.tonbrett.gradle.androidSdk
import dev.schlaubi.tonbrett.gradle.apiUrl
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.ksp)
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    targetHierarchy.default {
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
            }

            group("jvm") {
                withAndroidTarget()
                withJvm()
            }

            group("mobile") {
                withApple()
                withAndroidTarget()

            }
            group("nonDesktop") {
                withJs()
                withApple()
                withAndroidTarget()
            }
        }
    }
    androidTarget()
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
                implementation(libs.imageloader)
            }
        }

        named("jsMain") {
            dependencies {
                api("org.jetbrains.kotlin:kotlinx-atomicfu-runtime:1.9.0")
            }
        }

        named("jvmMain") {
            dependencies {
                implementation(libs.kmongo.id.serialization)
            }
        }

        named("mobileMain") {
            dependencies {
                api(libs.kvault)
            }
        }
    }
}

dependencies {
    kspCommonMainMetadata(libs.lyricist.processor)
    "jvmMainImplementation"(libs.kord.common) {
        exclude(group = "io.ktor")
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

tasks {
    afterEvaluate {
        named("lintAnalyzeDebug") {
            dependsOn(
                "generateAndroidUnitTestDebugNonAndroidBuildConfig",
                "generateAndroidUnitTestNonAndroidBuildConfig"
            )
        }
        val compilationTasks = kotlin.targets.flatMap {
            buildList {
                if (it.name != "android") {
                    add("compileKotlin${it.name.capitalized()}")
                    val sourcesJarName = "${it.name}SourcesJar"
                    add(sourcesJarName)
                } else {
                    add("compileDebugKotlinAndroid")
                    add("compileReleaseKotlinAndroid")
                }
            }
        }
        for (task in compilationTasks) {
            named(task) {
                dependsOn("kspCommonMainKotlinMetadata")
            }
        }
    }
}
