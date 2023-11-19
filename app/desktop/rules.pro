# Ktor
-keepclassmembers class io.ktor.** { volatile <fields>; }
-keep class io.ktor.client.engine.okhttp.OkHttpEngineContainer
-keep class io.ktor.serialization.kotlinx.json.KotlinxSerializationJsonExtensionProvider
-keep class kotlinx.coroutines.** { *; }
# even though we don't use log4j, proguard fails preverification if this class gets optimized
-keep class io.netty.util.internal.logging.Log4J2LoggerFactory { *; }
-keepattributes Signature,InnerClasses
-keepclasseswithmembers class io.netty.** {
    *;
}
-keepnames class io.netty.** {
    *;
}
-dontwarn kotlinx.atomicfu.**
-dontwarn io.netty.**
-dontwarn com.typesafe.**
-dontwarn org.slf4j.**
-dontwarn ch.qos.logback.**
-dontwarn okhttp3.**
-dontwarn io.ktor.**
-dontwarn dev.schlaubi.tonbrett.app.desktop.uwp_helper.**
-dontwarn io.github.g00fy2.versioncompare.**

# kmongo
-keep class org.litote.kmongo.id.UUIDStringIdGeneratorProvider
-keepclassmembers class org.litote.kmongo.id.StringId {
    public <init>(java.lang.String);
}

# logback
-keep class ch.qos.logback.classic.spi.LogbackServiceProvider
-keep class ch.qos.logback.core.rolling.RollingFileAppender
-keep class ch.qos.logback.core.rolling.TimeBasedRollingPolicy
-keep class ch.qos.logback.core.ConsoleAppender

# serialization
# For some reason if we don't do this, we get a VerifyError at runtime
-keep class kotlinx.serialization.* { *; }

# Serializer for classes with named companion objects are retrieved using `getDeclaredClasses`.
# If you have any, replace classes with those containing named companion objects.
-keepattributes InnerClasses # Needed for `getDeclaredClasses`.

# Kotlin serialization looks up the generated serializer classes through a function on companion
# objects. The companions are looked up reflectively so we need to explicitly keep these functions.
-keepclasseswithmembers class **.*$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
# If a companion has the serializer function, keep the companion field on the original type so that
# the reflective lookup succeeds.
-if class **.*$Companion {
  kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class <1>.<2> {
  <1>.<2>$Companion Companion;
}

# compose
-keep class androidx.compose.ui.text.platform.Platform { *; }
-keep class androidx.compose.runtime.** { *; }
