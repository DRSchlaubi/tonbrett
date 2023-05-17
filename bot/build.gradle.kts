import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.ksp)
    kotlin("jvm")
    alias(libs.plugins.mikbot)
}

dependencies {
    implementation(projects.common)
    implementation(libs.ktor.server.auth) {
        exclude(module = "ktor-server-core")
    }
    implementation(libs.ktor.server.websockets) {
        exclude(module = "ktor-server-core")
    }
    implementation(libs.kmongo.id.serialization)
    plugin(libs.mikbot.ktor)
    plugin(libs.mikbot.music)
    ksp(libs.kordex.processor)
}

mikbotPlugin {
    provider = "Schlaubi"
    pluginId = "tonbrett"
    license = "MIT"
}

tasks {
    installBot {
        botVersion = "3.17.5"
    }

    assembleBot {
        bundledPlugins = listOf(
            "ktor@${libs.versions.mikbot.get()}",
            "music@${libs.mikbot.music.get().version}"
        )
    }

    withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-Xcontext-receivers")
        }
    }
}

sourceSets {
    main {
        java {
            srcDir(file("$buildDir/generated/ksp/main/kotlin"))
        }
    }
}
