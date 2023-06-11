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

kotlin {
    jvmToolchain(19)
}
