import org.gradle.kotlin.dsl.`maven-publish`
import org.gradle.kotlin.dsl.signing
import java.util.Base64

plugins {
    `maven-publish`
    signing
}

publishing {
    repositories {
        maven("https://schlaubi.jfrog.io/artifactory/mikbot/") {
            credentials {
                username = System.getenv("JFROG_USER")
                password = System.getenv("JFROG_PASSWORD")
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
