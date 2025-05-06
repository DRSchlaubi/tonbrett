package dev.schlaubi.tonbrett.gradle

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.JavaVersion

const val sdkInt = 35
const val androidSdk = "android-$sdkInt"

val jvmTarget = JvmTarget.JVM_23
val javaVersion = JavaVersion.toVersion(jvmTarget.target)
