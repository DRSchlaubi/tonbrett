import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.ksp)
    kotlin("jvm")
    kotlin("plugin.serialization")
    alias(libs.plugins.mikbot)
}

repositories {
    mavenLocal()
}

dependencies {
    implementation(projects.common)
    ktorDependency(libs.ktor.server.auth)
    ktorDependency(libs.ktor.server.websockets)
    ktorDependency(libs.ktor.server.cors)
    ktorDependency(libs.ktor.server.auth.jwt)
    implementation(libs.kmongo.id.serialization)
    plugin(libs.mikbot.ktor)
    plugin(libs.mikbot.music)
    ksp(libs.kordex.processor)
}

fun DependencyHandlerScope.ktorDependency(dependency: ProviderConvertible<*>) = ktorDependency(dependency.asProvider())
fun DependencyHandlerScope.ktorDependency(dependency: Provider<*>) = implementation(dependency) {
    exclude(module = "ktor-server-core")
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

    val buildWebApp = register<Copy>("buildWebApp") {
        val webApp = project(":app:web")
        from(webApp.tasks.getByName("jsBrowserDistribution"))
        into(buildDir.resolve("resources").resolve("main").resolve("web"))
    }

    classes {
        dependsOn(buildWebApp)
    }
}

sourceSets {
    main {
        java {
            srcDir(file("$buildDir/generated/ksp/metadata/commonMain/kotlin"))
        }
    }
}
