import dev.schlaubi.tonbrett.gradle.sdkInt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("android")
    id("com.android.application")
    id("org.jetbrains.compose")
}

dependencies {
    implementation(projects.app.shared)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core)
    implementation(libs.androidx.browser)
    implementation(libs.mdc.android)
    implementation(libs.androidx.constraintlayout)
}

android {
    namespace = "dev.schlaubi.tonbrett.app.android"
    compileSdk = sdkInt
    defaultConfig {
        applicationId = "dev.schlaubi.tonbrett.android"
        minSdk = 26
        targetSdk = sdkInt
        versionCode = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull()?.plus(931356) ?: 1
        versionName = rootProject.version.toString()
    }

    buildTypes {
        release {
            applicationVariants.all {
                outputs.all {
                    archivesName = "tonbrett-app"
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        compose = true
    }
}

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }
}
