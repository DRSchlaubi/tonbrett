import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(libs.google.artifactregistry)
    implementation(libs.kotlinx.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }
}
