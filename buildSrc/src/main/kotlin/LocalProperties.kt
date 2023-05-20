package dev.schlaubi.tonbrett.gradle

import org.gradle.api.Project
import java.util.*

val Project.apiUrl: String
    get() {
        val file = rootProject.file("local.properties")
        if (file.exists()) {
            val properties = Properties().apply {
                load(file.bufferedReader())
            }

            val localUrl = properties.getProperty("api.url")
            if(!localUrl.isNullOrBlank()) {
                return localUrl
            }
        }
        return "https://musikus.gutikus.schlau.bi"
    }
