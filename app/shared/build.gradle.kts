plugins {
    kotlin("multiplatform")
    //id("com.android.library")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
    alias(libs.plugins.ksp)
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
            kotlin.srcDir(file("$buildDir/generated/ksp/metadata/commonMain/kotlin"))
            dependencies {
                api(projects.client)
                api(libs.lyricist)
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
