import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
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

            group("jvm") {
                withJvm()
                withAndroidTarget()
            }
        }
    }

    androidTarget {
        compilerOptions {
            jvmTarget = dev.schlaubi.tonbrett.gradle.jvmTarget
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
    targetCompatibility = JavaVersion.VERSION_22
}

android {
    compileOptions {
        targetCompatibility = JavaVersion.VERSION_22
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}
