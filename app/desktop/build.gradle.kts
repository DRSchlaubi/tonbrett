import dev.schlaubi.tonbrett.gradle.javaVersion
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
    alias(libs.plugins.kotlin.compose)
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
}

val windowsBuild = HostManager.hostIsMingw && rootProject.property("dev.schlaubi.tonbrett.no_windows") != "true"

dependencies {
    implementation(projects.app.shared)
    implementation(project.dependencies.compose.desktop.currentOs)
    implementation(project.dependencies.compose.materialIconsExtended)
    implementation(project.dependencies.compose.material3)
    implementation(libs.logback)
    implementation(compose.components.resources)

    if (windowsBuild) {
        implementation(projects.app.desktop.uwpHelper)
    } else {
        implementation(libs.ktor.server.netty)
        implementation(libs.ktor.server.cors)
    }
}

sourceSets {
    main {
        kotlin.srcDir("build/generated/compose/resourceGenerator/kotlin/commonResClass")
        kotlin.srcDir("build/generated/compose/resourceGenerator/kotlin/mainResourceAccessors")
        if (windowsBuild) {
            kotlin.srcDir("src/windowsMain/kotlin")
        } else {
            kotlin.srcDir("src/nonWindowsMain/kotlin")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = dev.schlaubi.tonbrett.gradle.jvmTarget
    }
}

java {
    sourceCompatibility = javaVersion
}

compose.desktop {
    application {
        mainClass = "dev.schlaubi.tonbrett.app.desktop.MainKt"

        jvmArgs("--enable-native-access=ALL-UNNAMED", "--enable-preview")
        nativeDistributions {
            modules(
                "java.naming", // required by logback
                "java.net.http" // Http client
            )
            when {
                HostManager.hostIsMingw -> targetFormats(TargetFormat.AppImage)
                HostManager.hostIsMac -> targetFormats(TargetFormat.Pkg)
                else -> targetFormats(TargetFormat.Deb, TargetFormat.Rpm)
            }

            licenseFile = rootProject.file("LICENSE")
            vendor = "Schlaubi"
            description = "Companion App for Discord Tonbrett bot"
            copyright = "(c) 2025 Michael Rittmeister"

            packageName = "Tonbrett"
            packageVersion = project.version.toString()

            windows {
                iconFile = rootProject.file("icons/logo.ico")
                menuGroup = "Tonbrett"
                upgradeUuid = "c8bce0ed-113c-4d78-879b-78ed5c7d9f7f"
            }

            linux {
                iconFile = rootProject.file("icons/logo.png")
            }

            macOS {
                iconFile = rootProject.file("icons/logo.icns")
                bundleID = "dev.schlaubi.tonbrett.ios"
                entitlementsFile.set(file("entitlements.entitlements"))
                appStore = false
                signing {
                    identity.set("BBN Holding Inc.")
                }
                notarization {
                    appleID.set("michael@rittmeister.in")
                    password.set(System.getenv("NOTARIZATION_PASSWORD"))
                    teamID.set("H8563U643B")
                }
            }
        }

        buildTypes {
            release {
                proguard {
                    version = "7.7.0"
                    configurationFiles.from(files("rules.pro"))
                }
            }
        }
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = "dev.schlaubi.tonbrett.app.desktop"
    customDirectory(
        sourceSetName = "main",
        directoryProvider = provider { layout.projectDirectory.dir("src/main/composeResources") }
    )
}

tasks {
    register<Tar>("packageDistributable") {
        from(named("createReleaseDistributable"))
        archiveBaseName = "tonbrett"
        archiveClassifier = "linux-${System.getProperty("os.arch")}"
        compression = Compression.GZIP
        archiveExtension = "tar.gz"
    }

    if (true) {
        registerMsixTasks()
        registerMsixTasks("MSStore", msStore = true)
    }

    withType<JavaCompile> {
        options.compilerArgs.add("--enable-preview")
    }
}

fun TaskContainerScope.registerMsixTasks(prefix: String = "", msStore: Boolean = false) {
    val dirName = if (prefix.isNotBlank()) {
        "$prefix-msix-workspace"
    } else {
        "msix-workspace"
    }
    val targetDir = layout.buildDirectory.dir(dirName)

    val prepareUwpWorkspace = register<Copy>("prepare${prefix}UwpWorkspace") {
        dependsOn("uwp_helper:compileRust")
        from(named("packageReleaseAppImage")) {
            eachFile {
                path = path.substringAfter("Tonbrett")
            }
        }
        from(file("uwp_helper/target/release")) {
            include("*.dll")
        }
        from(file("msix"))
        into(targetDir)
    }

    val updateMsixVersion = register<Exec>("update${prefix}MsixVersion") {
        inputs.property("version", project.version)
        val dir = targetDir.get()
        outputs.file(dir.file("appxmanifest.xml"))
        dependsOn(prepareUwpWorkspace)

        workingDir = dir.asFile
        val script = dir.file("update_msix_version.ps1")
        val command = buildString {
            append("Powershell -File ${script.asFile.absolutePath} -Version ${project.version}.0")
            if (msStore) {
                append(" -IsMsix true")
            }
            val arch =System.getProperty("os.arch")
            if (arch.contains("arm") || arch.contains("aarch")) {
                append(" -IsArm true")
            }
        }
        commandLine("cmd", "/c", command)
    }

    val finalizeMsixWorkspace = register<Delete>("finalize${prefix}MsixWorkspace") {
        dependsOn(updateMsixVersion)
        delete(targetDir.get().file("update_msix_version.ps1"))
    }

    afterEvaluate {
        "packageReleaseDistributionForCurrentOS" {
            if (HostManager.hostIsMingw) {
                dependsOn(finalizeMsixWorkspace)
            }
        }
    }
}
