import org.gradle.api.artifacts.Dependency

fun Dependency.toNotation() = "$group:$name:$version"
