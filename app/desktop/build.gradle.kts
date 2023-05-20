import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.org.jline.utils.OSUtils

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

dependencies {
    implementation(projects.app.shared)
    implementation(compose.desktop.currentOs)
    implementation(libs.logback)
    implementation(libs.ktor.server.netty)
}


compose.desktop {
    application {
        mainClass = "dev.schlaubi.tonbrett.app.desktop.MainKt"

        nativeDistributions {
            modules(
                "java.naming" // required by logback
            )
            when {
                OSUtils.IS_WINDOWS -> targetFormats(TargetFormat.Msi)
                OSUtils.IS_OSX -> targetFormats(TargetFormat.Dmg)
                else -> targetFormats(TargetFormat.Deb)
            }

            licenseFile = rootProject.file("LICENSE")

            packageName = "Tonbrett"
            packageVersion = project.version.toString()

            windows {
                menuGroup = "Tonbrett"
                upgradeUuid = "c8bce0ed-113c-4d78-879b-78ed5c7d9f7f"
            }
        }

        buildTypes {
            release {
                proguard {
                    isEnabled = false
                }
            }
        }
    }
}

