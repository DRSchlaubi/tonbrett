import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask
import org.jetbrains.kotlin.org.jline.utils.OSUtils
import kotlin.io.path.pathString

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

dependencies {
    implementation(projects.app.shared)
    implementation(compose.desktop.currentOs)
    implementation(libs.logback)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.cors)
}


compose.desktop {
    application {
        mainClass = "dev.schlaubi.tonbrett.app.desktop.MainKt"

        nativeDistributions {
            modules(
                "java.naming" // required by logback
            )
            when {
                OSUtils.IS_WINDOWS -> targetFormats(TargetFormat.AppImage)
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
                iconFile.set(rootProject.file("icons/logo.ico"))
                menuGroup = "Tonbrett"
                upgradeUuid = "c8bce0ed-113c-4d78-879b-78ed5c7d9f7f"
            }

            macOS {
                iconFile.set(rootProject.file("icons/logo.icns"))
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
                    version = "7.3.2"
                    configurationFiles.from(files("rules.pro"))
                }
            }
        }
    }
}

tasks {
    register<Tar>("packageDistributable") {
        from(named("createReleaseDistributable"))
        archiveBaseName = "tonbrett"
        archiveClassifier = "linux"
        compression = Compression.GZIP
        archiveExtension = "tar.gz"
    }

    val buildUwpHelper by creating(Exec::class) {
        inputs.dir("uwp_helper/src")
        outputs.dir("uwp_helper/target")

        workingDir = file("uwp_helper")
        commandLine("cargo", "build", "--release")
    }

    val prepareUwpWorkspace by creating(Copy::class) {
        dependsOn(buildUwpHelper)
        afterEvaluate {
            from(named("packageReleaseAppImage")) {
                eachFile {
                    path = path.substringAfter("Tonbrett")
                }
            }
        }
        from(file("uwp_helper/target/release")) {
            include("*.exe")
        }
        from(file("msix"))
        into(buildDir.resolve("msix-workspace"))
    }

    afterEvaluate {
        "packageReleaseDistributionForCurrentOS" {
            if (OSUtils.IS_WINDOWS) {
                dependsOn(prepareUwpWorkspace)
            }
        }
    }
}
