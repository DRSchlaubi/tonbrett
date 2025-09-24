import dev.schlaubi.tonbrett.gradle.sdkInt
import dev.schlaubi.tonbrett.gradle.withAndroidMP
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    com.android.kotlin.multiplatform.library
}

@OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)
kotlin {
    applyDefaultHierarchyTemplate {
        common {
            group("nonJvm") {
                withNative()
                withJs()
                withWasmJs()
            }

            group("java") {
                withJvm()
                withAndroidTarget()
                withAndroidMP()
            }
        }
    }

    androidLibrary {
        compileSdk = sdkInt
        namespace = "dev.schlaubi.tonbrett.${project.name}"

        compilerOptions {
            jvmTarget = dev.schlaubi.tonbrett.gradle.jvmTarget
        }

        lint {
            checkReleaseBuilds = false
            abortOnError = false
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget = dev.schlaubi.tonbrett.gradle.jvmTarget
        }
    }
    wasmJs {
        browser()
    }
    iosSimulatorArm64()
    iosX64()
    iosArm64()

    targets.all {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }
        }
    }
}

java {
    targetCompatibility = JavaVersion.VERSION_24
}
