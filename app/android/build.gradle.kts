import dev.schlaubi.tonbrett.gradle.sdkInt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.compose)
    kotlin("android")
    id("com.android.application")
    id("org.jetbrains.compose")
}

repositories {
    maven("https://androidx.dev/storage/compose-compiler/repository/")
}

dependencies {
    implementation(projects.app.shared)
    implementation(projects.app.android.androidShared)
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

    implementation(libs.horologist.datalayer)
    implementation(libs.horologist.auth.data.phone)
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
        versionCode = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull()?.plus(932054) ?: 932054
        versionName = rootProject.version.toString()
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            pickFirsts.add("META-INF/versions/9/previous-compilation-data.bin")
            pickFirsts.add("META-INF/native-image/org.mongodb/bson/native-image.properties")
        }
    }
    androidResources {
        generateLocaleConfig = true
    }

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_22
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_22
        }
    }
}
