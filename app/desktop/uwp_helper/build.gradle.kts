import org.jetbrains.kotlin.org.jline.utils.OSUtils

plugins {
    java
}

val jextractOutput: Provider<Directory> = layout.buildDirectory.dir("generated/jextract/main/java")

sourceSets {
    main {
        java.srcDir(jextractOutput)
    }
}

tasks {
    val compileRust by registering(Exec::class) {
        inputs.dir("src")
        outputs.dir("target")

        commandLine("cargo", "build", "--release")
    }

    val generateBindingsWithJextract by registering(Exec::class) {
        dependsOn(compileRust)
        val header = "target/uwp_helper.h"
        inputs.file(header)
        outputs.dir(jextractOutput)

        // I cannot figure out how to change the path on GitHub Actions
        val command = if (OSUtils.IS_WINDOWS) {
            "jextract.bat"
        } else {
            "jextract"
        }

        val libraryPath = if (System.getenv("GITHUB_REF") != null) {
            "uwp_helper"
        } else {
            file("target/release/uwp_helper").absolutePath
        }

        commandLine(
            command,
            "--header-class-name", "UwpHelper",
            "--target-package", "dev.schlaubi.tonbrett.app.desktop.uwp_helper",
            "--library", libraryPath,
            "--output", jextractOutput.get().asFile.absolutePath,
            "--include-function", "launch_uri",
            "--include-function", "get_token",
            "--include-function", "store_token",
            "--include-function", "get_temp_folder",
            "--include-function", "copy_string_from_get_string_result_into_buffer",
            "--include-function", "request_msstore_auto_update",
            "--include-struct", "StringResult",
            "--include-typedef", "uint16_t",
            header,
        )
    }

    compileJava {
        dependsOn(generateBindingsWithJextract)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_22
}
