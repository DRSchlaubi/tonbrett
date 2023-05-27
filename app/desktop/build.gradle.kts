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
                OSUtils.IS_OSX -> targetFormats(TargetFormat.Pkg)
                else -> targetFormats(TargetFormat.Deb)
            }

            licenseFile = rootProject.file("LICENSE")
            vendor = "Schlaubi"
            description = "Companion App for Discord Tonbrett bot"
            copyright = "(c) 2023 Michael Rittmeister"

            packageName = "Tonbrett"
            packageVersion = project.version.toString()

            windows {
                iconFile.set(file("logo.ico"))
                menuGroup = "Tonbrett"
                upgradeUuid = "c8bce0ed-113c-4d78-879b-78ed5c7d9f7f"
            }

            macOS {
                iconFile.set(file("logo.icns"))
                bundleID = "dev.schlaubi.tonbrett.app.mac"
                entitlementsFile.set(file("entitlements.entitlements"))
                appStore = false
                signing {
                    identity = "Michael Rittmeister"
                }
                notarization {
                    appleID.set("michael@rittmeister.in")
                    password.set(System.getenv("NOTARIZATION_PASSWORD"))
                }
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

