import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
        compilations.all {
            compilerOptions.configure {
                freeCompilerArgs.addAll(
                    "-Xir-per-module", "-Xir-property-lazy-initialization"
                )
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
        dependsOn("jsBrowserProductionExecutableDistributeResources")
        from(rootProject.file("icons/logo.ico"))
        into(buildDir.resolve("distribuions"))
        rename { "favicon.ico" }
    }

    named("jsMainClasses") {
        dependsOn(copyIcon)
    }
}

compose.experimental {
    web.application {}
}
