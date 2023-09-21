import dev.schlaubi.tonbrett.gradle.sdkInt

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
    implementation(compose.materialIconsExtended)
    implementation(libs.kotlinx.coroutines.play.services)
}

base {
    archivesName = "tonbrett-app"
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
        }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.2"
    }
    packaging {
        resources {
            pickFirsts.add("META-INF/versions/9/previous-compilation-data.bin")
        }
    }
}
