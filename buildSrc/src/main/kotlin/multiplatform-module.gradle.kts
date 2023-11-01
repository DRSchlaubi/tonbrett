import com.android.build.api.dsl.Lint
import com.android.build.gradle.internal.tasks.LintCompile
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    applyDefaultHierarchyTemplate {
        common {
            group("nonJvm") {
                withNative()
                withJs()
            }

            group("jvm") {
                withJvm()
                withAndroidTarget()
            }
        }
    }

    androidTarget {
        compilations.all {
            compilerOptions {
                jvmTarget = JvmTarget.JVM_20
            }
        }
    }
    jvm("desktop")
    js(IR) {
        browser()
    }
    iosSimulatorArm64()
    iosX64()
    iosArm64()
}

java {
    targetCompatibility = JavaVersion.VERSION_20
}

android {
    compileOptions {
        targetCompatibility = JavaVersion.VERSION_20
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}
