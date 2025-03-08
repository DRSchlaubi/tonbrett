plugins {
    alias(libs.plugins.kotlin.compose)
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("org.jetbrains.compose")
}

kotlin {
    applyDefaultHierarchyTemplate()
    iosX64()
    iosSimulatorArm64()
    iosArm64()

    cocoapods {
        version = project.version.toString()
        summary = "Tonbrett iOS app"
        homepage = "https://github.com/DRSchlaubi/tonbrett"
        ios.deploymentTarget = "17.6"
        podfile = project.file("Podfile")
        framework {
            baseName = "TonbrettShared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.app.shared)
                implementation(compose.runtime)
                implementation(compose.foundation)
            }
        }
    }
}