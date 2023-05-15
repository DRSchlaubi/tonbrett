plugins {
    alias(libs.plugins.kotlin.multiplatform)
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
                api(libs.ktor.resources)
            }
        }

        named("jvmMain") {
            dependencies {
                implementation(libs.kmongo.id.serialization)
                implementation(libs.kord.common)
            }
        }
    }
}
