import dev.schlaubi.tonbrett.gradle.androidSdk

plugins {
    `multiplatform-module`
    kotlin("plugin.serialization")
    `published-module`
}

kotlin {
    explicitApi()

    macosArm64()
    macosX64()
    mingwX64()

    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.serialization)
                compileOnly(libs.ktor.resources)
            }
        }

        jvmMain {
            dependencies {
                compileOnly(libs.kmongo.id.serialization)
                compileOnly(libs.kord.common)
                compileOnly(libs.kmongo.serialization)
            }
        }
    }
}

android {
    namespace = "dev.schlaubi.tonbrett.common"
    compileSdkVersion = androidSdk
}
