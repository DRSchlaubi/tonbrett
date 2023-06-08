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
    compileOnly(kotlin("gradle-plugin"))
    implementation(libs.google.artifactregistry)
}

java {
    sourceCompatibility = JavaVersion.VERSION_19
}

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            //jvmTarget = JvmTarget.JVM_19
        }
    }
}
