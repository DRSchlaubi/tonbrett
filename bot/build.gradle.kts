 import dev.schlaubi.mikbot.gradle.mikbot
 import dev.schlaubi.tonbrett.gradle.javaVersion
 import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.ksp)
    kotlin("jvm")
    kotlin("plugin.serialization")
    alias(libs.plugins.mikbot)
}

repositories {
    maven("https://maven.topi.wtf/releases")
}

dependencies {
    implementation(projects.common)
    ktorDependency(libs.ktor.server.auth)
    ktorDependency(libs.ktor.server.cors)
    ktorDependency(libs.ktor.server.auth.jwt)
    ktorDependency(libs.ktor.server.compression)
    implementation(libs.kmongo.id.serialization)
    plugin(mikbot(libs.mikbot.ktor))
    plugin(libs.mikbot.music)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xdont-warn-on-error-suppression")
    }
}

java {
    sourceCompatibility = javaVersion
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

    i18n {
        classPackage = "dev.schlaubi.tonbrett.bot.translations"
        translationBundle = "soundboard"
        className = "SoundboardTranslations"
    }
}

tasks {
    val buildWebApp = register<Copy>("buildWebApp") {
        val webApp = project(":app:web")
        from(webApp.tasks.named("wasmJsBrowserDistribution"))
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
