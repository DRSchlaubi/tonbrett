import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(kotlin("gradle-plugin"))
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
