import org.gradle.kotlin.dsl.`maven-publish`
import org.gradle.kotlin.dsl.signing
import java.util.Base64

plugins {
    `maven-publish`
    signing
    com.google.cloud.artifactregistry.`gradle-plugin`
}

publishing {
    repositories {
        maven("artifactregistry://europe-west3-maven.pkg.dev/mik-music/mikbot") {
            credentials {
                username = "_json_key_base64"
                password = System.getenv("GOOGLE_KEY")
            }

            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

signing {
    val key = System.getenv("SIGNING_KEY")
    val password = System.getenv("SIGNING_PASSWORD")
    if(key != null && password != null) {
        useInMemoryPgpKeys(String(Base64.getDecoder().decode(key)), password)
        sign(publishing.publications)
    }
}
