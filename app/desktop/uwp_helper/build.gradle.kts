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
        val command = if(System.getenv("GITHUB_RUN_NUMBER") != null) {
            rootProject.file("jextract-20/bin/jextract.bat").absolutePath
        } else {
            "jextract"
        }

        commandLine(
            command,
            "--header-class-name", "UwpHelper",
            "--target-package", "dev.schlaubi.tonbrett.app.desktop.uwp_helper",
            "--library", "uwp_helper",
            "--output", jextractOutput.get().asFile.absolutePath,
            "--source",
            "--include-function", "launch_uri",
            "--include-function", "get_app_data_roaming",
            "--include-function", "copy_string_from_get_app_data_roaming_result_into_buffer",
            "--include-struct", "AppDataRoamingResult",
            "--include-typedef", "uint16_t",
            header,
        )
    }

    compileJava {
        dependsOn(generateBindingsWithJextract)
        options.compilerArgs.add("--enable-preview")
    }
}
