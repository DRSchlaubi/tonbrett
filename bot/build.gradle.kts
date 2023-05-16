plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.mikbot)
}

dependencies {
    implementation(projects.common)
    plugin(libs.mikbot.ktor)
    plugin(libs.mikbot.music)
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

    assembleBot {
        bundledPlugins = listOf(
            "ktor@${libs.versions.mikbot.get()}",
            "music@${libs.mikbot.music.get().version}"
        )
    }
}
