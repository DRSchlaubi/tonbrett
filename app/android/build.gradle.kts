import dev.schlaubi.tonbrett.gradle.sdkInt
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName

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
    implementation(libs.google.play)
    implementation(libs.google.play.ktx)
    implementation(compose.material3)
    implementation(libs.kotlinx.coroutines.play.services)
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
            isMinifyEnabled = true
            proguardFile("proguard-rules.pro")

            applicationVariants.all {
                outputs.all {
                    archivesName = "tonbrett-app"
                }
            }
        }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            pickFirsts.add("META-INF/versions/9/previous-compilation-data.bin")
        }
    }
}
