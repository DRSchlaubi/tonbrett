import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
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
                @OptIn(ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended)
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

compose.experimental {
    web.application {}
}
