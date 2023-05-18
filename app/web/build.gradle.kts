import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
        compilations.all {
            compilerOptions.options.freeCompilerArgs.addAll(
                "-Xir-per-module", "-Xir-property-lazy-initialization")
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("androidx.compose.ui.ExperimentalComposeUiApi")
        }

        commonMain {
            dependencies {
                implementation(projects.app.shared)
                implementation(compose.runtime)
                implementation(compose.ui) {
                    version {
                        strictly("1.4.0-dev-wasm05")
                    }
                }
                implementation(compose.foundation)
                implementation(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
            }
        }
    }
}

compose.experimental {
    web.application {}
}

rootProject.the<NodeJsRootExtension>().versions.webpack.version = "5.76.2"
