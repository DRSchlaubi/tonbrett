import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
    google()
}

dependencies {
    implementation(libs.google.artifactregistry)
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.android.gradle.plugin)
}

java {
    sourceCompatibility = JavaVersion.VERSION_24
}

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_24
        }
    }
}
