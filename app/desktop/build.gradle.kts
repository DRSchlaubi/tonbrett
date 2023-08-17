import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.org.jline.utils.OSUtils

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
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

        jvmArgs("--enable-native-access=ALL-UNNAMED", "--enable-preview")
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
                iconFile = rootProject.file("icons/logo.ico")
                menuGroup = "Tonbrett"
                upgradeUuid = "c8bce0ed-113c-4d78-879b-78ed5c7d9f7f"
            }

            macOS {
                iconFile = rootProject.file("icons/logo.icns")
                bundleID = "dev.schlaubi.tonbrett.app.mac"
                entitlementsFile.set(file("entitlements.entitlements"))
                appStore = false
                signing {
                    identity = "Michael Rittmeister"
                }
                notarization {
                    appleID = "michael@rittmeister.in"
                    password = System.getenv("NOTARIZATION_PASSWORD")
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

    val buildUwpHelper by registering(Exec::class) {
        inputs.dir("uwp_helper/src")
        outputs.dir("uwp_helper/target")

        workingDir = file("uwp_helper")
        commandLine("cargo", "build", "--release")
    }

    val prepareUwpWorkspace by registering(Copy::class) {
        dependsOn(buildUwpHelper)
        from(named("packageReleaseAppImage")) {
            eachFile {
                path = path.substringAfter("Tonbrett")
            }
        }
        from(file("uwp_helper/target/release")) {
            include("*.dll")
        }
        from(file("msix"))
        into(layout.buildDirectory.dir("msix-workspace"))
    }

    val updateMsixVersion by registering(Exec::class) {
        inputs.property("version", project.version)
        val dir = layout.buildDirectory.dir("msix-workspace").get()
        outputs.file(dir.file("appxmanifest.xml"))
        dependsOn(prepareUwpWorkspace)

        workingDir = dir.asFile
        val script = dir.file("update_msix_version.ps1")
        commandLine("cmd", "/c", "Powershell -File ${script.asFile.absolutePath} -Version ${project.version}.0")
    }

    val finalizeMsixWorkspace by registering(Delete::class) {
        dependsOn(updateMsixVersion)
        delete(layout.buildDirectory.file("msix-workspace/update_msix_version.ps1"))
    }

    withType<JavaCompile> {
        options.compilerArgs.add("--enable-preview")
    }

    afterEvaluate {
        "packageReleaseDistributionForCurrentOS" {
            if (OSUtils.IS_WINDOWS) {
                dependsOn(finalizeMsixWorkspace)
            }
        }
    }
}
