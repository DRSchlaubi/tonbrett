import dev.schlaubi.mikbot.gradle.mikbot
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.ksp)
    kotlin("jvm")
    kotlin("plugin.serialization")
    alias(libs.plugins.mikbot)
}

dependencies {
    implementation(projects.common)
    ktorDependency(libs.ktor.server.auth)
    ktorDependency(libs.ktor.server.cors)
    ktorDependency(libs.ktor.server.auth.jwt)
    implementation(libs.kmongo.id.serialization)
    plugin(mikbot(libs.mikbot.ktor))
    plugin(libs.mikbot.music)
}

kotlin {
    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_0
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}

fun DependencyHandlerScope.ktorDependency(dependency: ProviderConvertible<*>) = ktorDependency(dependency.asProvider())
fun DependencyHandlerScope.ktorDependency(dependency: Provider<*>) = implementation(dependency) {
    exclude(module = "ktor-server-core")
}

mikbotPlugin {
    provider = "Schlaubi"
    pluginId = "tonbrett"
    license = "MIT"
    enableKordexProcessor = true
}

tasks {
    assembleBot {
        bundledPlugins = listOf(
            "ktor@1.25.0",
            "music-player@${libs.mikbot.music.get().version}"
        )
    }

    val buildWebApp = register<Copy>("buildWebApp") {
        val webApp = project(":app:web")
        from(webApp.tasks.named("jsBrowserDistribution"))
        into(layout.buildDirectory.dir("resources/main/web"))
    }

    classes {
        dependsOn(buildWebApp)
    }
}

sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("/generated/ksp/metadata/commonMain/kotlin"))
        }
    }
}
