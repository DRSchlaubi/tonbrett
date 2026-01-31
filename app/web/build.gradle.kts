import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.compose)
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap/")
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            webpackTask {
                mainOutputFileName = ".proxy/web.js"
            }
        }
        binaries.executable()
        compilations.all {
            packageJson {
                devDependencies += "html-webpack-plugin" to "5.6.0"
                devDependencies += "browserify" to "17.0.0"
            }
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.addAll(
                        "-Xir-per-module", "-Xir-property-lazy-initialization"
                    )
                }
            }
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("androidx.compose.ui.ExperimentalComposeUiApi")
        }

        commonMain {
            dependencies {
                implementation(projects.app.shared)
                implementation(libs.compose.runtime)
                implementation(libs.compose.ui)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.material.icons.extended)
                implementation(npm("@discord/embedded-app-sdk", "2.4.0"))
            }
        }
    }

    compilerOptions {
        optIn.add("kotlin.js.ExperimentalWasmJsInterop")
    }
}

tasks {
     val copyIcon by registering(Copy::class) {
        from(rootProject.file("icons/logo.ico"))
        into(layout.buildDirectory.dir("dist/wasmJs/productionExecutable"))
        rename { "favicon.ico" }
    }

    named("wasmJsMainClasses") {
        dependsOn(copyIcon)
    }
}
