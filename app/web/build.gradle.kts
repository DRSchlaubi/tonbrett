plugins {
    alias(libs.plugins.kotlin.compose)
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap/")
}

kotlin {
    js(IR) {
        browser()
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
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended)
                implementation(npm("@discord/embedded-app-sdk", "1.4.2"))
            }
        }
    }
}

tasks {
    val copyIcon by creating(Copy::class) {
        from(rootProject.file("icons/logo.ico"))
        into(layout.buildDirectory.dir("distributions"))
        rename { "favicon.ico" }
    }

    named("jsMainClasses") {
        dependsOn(copyIcon)
    }
}
