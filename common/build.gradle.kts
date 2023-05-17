plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    explicitApi()

    jvm()
    js(IR) {
        browser()
    }

    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.serialization)
                compileOnly(libs.ktor.resources)
            }
        }

        named("jvmMain") {
            dependencies {
                compileOnly(libs.kmongo.id.serialization)
                compileOnly(libs.kord.common)
            }
        }
    }
}
