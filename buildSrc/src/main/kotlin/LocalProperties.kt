package dev.schlaubi.tonbrett.gradle

import org.gradle.api.Project
import java.util.*

val Project.apiUrl: String
    get() {
        val file = rootProject.file("local.properties")
        return if (file.exists()) {
            val properties = Properties().apply {
                load(file.bufferedReader())
            }

            properties.getProperty("api.url")
        } else {
            "https://musikus.gutikus.schlau.bi"
        }
    }
