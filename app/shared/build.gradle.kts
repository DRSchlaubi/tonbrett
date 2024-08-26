import dev.schlaubi.tonbrett.gradle.androidSdk
import dev.schlaubi.tonbrett.gradle.apiUrl
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    `multiplatform-module`
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.compose")
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.ksp)
}

// TODO: https://github.com/google/ksp/issues/1823
project.setProperty("ksp.useKSP2", false)

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
            }
        }

        jsMain {
            dependencies {
                api("org.jetbrains.kotlin:kotlinx-atomicfu-runtime:1.9.21")
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
    kspCommonMainMetadata(libs.lyricist.processor)
    "desktopMainImplementation"(libs.kord.common) {
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
//        named("lintAnalyzeDebug") {
//            dependsOn(
//                "generateAndroidUnitTestDebugNonAndroidBuildConfig",
//                "generateAndroidUnitTestNonAndroidBuildConfig"
//            )
//        }
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
