import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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
            targetFormats(TargetFormat.Deb, TargetFormat.AppImage, TargetFormat.Msi, TargetFormat.Deb)

            packageName = "Tonbrett"
            packageVersion = project.version.toString()

            windows {
                upgradeUuid = "c8bce0ed-113c-4d78-879b-78ed5c7d9f7f"
            }
        }
    }
}
