plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.mikbot)
}

dependencies {
    implementation(projects.common)
    plugin(libs.mikbot.ktor)
}

mikbotPlugin {
    provider = "Schlaubi"
    pluginId = "tonbrett"
    license = "MIT"
}

tasks {
    installBot {
        botVersion = "3.17.0"
    }
}
