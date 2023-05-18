plugins {
    kotlin("multiplatform")
    //id("com.android.library")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
}

kotlin {
    js(IR) {
        browser()
    }

    sourceSets {
        all {
            languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
        }
        commonMain {
            dependencies {
                api(projects.client)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.material3)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
            }
        }

        named("jsMain") {
            dependencies {
                api("org.jetbrains.kotlin:kotlinx-atomicfu-runtime:1.8.20")
            }
        }
    }
}
