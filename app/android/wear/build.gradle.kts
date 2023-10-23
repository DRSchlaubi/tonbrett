import dev.schlaubi.tonbrett.gradle.sdkInt

plugins {
    kotlin("android")
    id("com.android.application")
    id("org.jetbrains.compose")
}

dependencies {
    implementation(projects.app.shared)
    implementation(projects.app.android.androidShared)
    implementation(libs.androidx.activity)

    implementation(libs.androidx.core)

    implementation(libs.horologist.datalayer)
    implementation(libs.horologist.auth.data)

    implementation(libs.androidx.wear.compose.material)
    implementation(libs.androidx.wear.compose.foundation)
}

android {
    namespace = "dev.schlaubi.tonbrett.app.android.wear"
    compileSdk = sdkInt
    defaultConfig {
        applicationId = "dev.schlaubi.tonbrett.android"
        minSdk = 26
        targetSdk = sdkInt
        versionCode = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull()?.plus(932054) ?: 932054
        versionName = rootProject.version.toString()
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources {
            pickFirsts.add("META-INF/versions/9/previous-compilation-data.bin")
        }
    }
}