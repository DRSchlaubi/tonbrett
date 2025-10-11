package dev.schlaubi.tonbrett.gradle

import com.android.build.api.dsl.KotlinMultiplatformAndroidCompilation
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinHierarchyBuilder

const val sdkInt = 36
const val androidSdk = "android-$sdkInt"

val jvmTarget = JvmTarget.JVM_23
val javaVersion = JavaVersion.toVersion(jvmTarget.target)

@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun KotlinHierarchyBuilder.withAndroidMP() {
    withAndroidTarget()
    withCompilations { it is KotlinMultiplatformAndroidCompilation }
}
