import com.google.protobuf.gradle.id
import dev.schlaubi.tonbrett.gradle.sdkInt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    kotlin("android")
    alias(libs.plugins.protobuf)
}

repositories {
    google()
}

dependencies {
    api(libs.protobuf.javalite)
    api(libs.protobuf.kotlin.lite)
    api(libs.horologist.datalayer)
}

android {
    namespace = "dev.schlaubi.tonbrett.app.android.shared"
    compileSdk = sdkInt
}

protobuf {
    protoc {
        artifact = libs.protoc.get().toNotation()
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("java") {
                    option("lite")
                }
                id("kotlin") {
                    option("lite")
                }
            }
        }
    }
}


//tasks {
//    afterEvaluate {
//        named("compileDebugJavaWithJavac") {
//            dependsOn("generateReleaseProto")
//        }
//        named("compileDebugKotlin") {
//            dependsOn("generateReleaseProto")
//        }
//    }
//}
