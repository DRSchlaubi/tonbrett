import org.gradle.api.artifacts.MinimalExternalModuleDependency

fun MinimalExternalModuleDependency.toNotation() = "$group:$name:$version"
