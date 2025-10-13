# Ktor
-keepclassmembers class io.ktor.** { volatile <fields>; }
-keep class io.ktor.client.engine.okhttp.OkHttpEngineContainer
-keep class io.ktor.serialization.kotlinx.json.KotlinxSerializationJsonExtensionProvider
# even though we don't use log4j, proguard fails preverification if this class gets optimized
-keep class io.netty.util.internal.logging.Log4J2LoggerFactory { *; }
-dontwarn io.github.oshai.kotlinlogging.internal.**

# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class kotlinx.coroutines.swing.SwingDispatcherFactory {}

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclasseswithmembers class io.netty.** {
    *;
}
-keepnames class io.netty.** {
    *;
}
# Same story for the standard library's SafeContinuation that also uses AtomicReferenceFieldUpdater
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}

-dontwarn kotlinx.atomicfu.**
-dontwarn io.netty.**
-dontwarn com.typesafe.**
-dontwarn org.slf4j.**
-dontwarn ch.qos.logback.**
-dontwarn io.ktor.**
-dontwarn dev.schlaubi.tonbrett.app.desktop.uwp_helper.**

# kmongo
-keep class org.litote.kmongo.id.UUIDStringIdGeneratorProvider
-keepclassmembers class org.litote.kmongo.id.StringId {
    public <init>(java.lang.String);
}
# This isn't used so it can be eliminated
-dontwarn dev.schlaubi.tonbrett.common.IdSerializer

# logback
-keep class ch.qos.logback.classic.spi.LogbackServiceProvider { *; }
-keep class ch.qos.logback.classic.util.** { *; }
-keep class ch.qos.logback.core.rolling.RollingFileAppender
-keep class ch.qos.logback.core.rolling.TimeBasedRollingPolicy
-keep class ch.qos.logback.core.ConsoleAppender

# serialization
# For some reason if we don't do this, we get a VerifyError at runtime
# Serializer for classes with named companion objects are retrieved using `getDeclaredClasses`.
# If you have any, replace classes with those containing named companion objects.
-keepattributes InnerClasses # Needed for `getDeclaredClasses`.

# Kotlin serialization looks up the generated serializer classes through a function on companion
# objects. The companions are looked up reflectively so we need to explicitly keep these functions.
-keepclasseswithmembers class **.*$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
# https://github.com/Kotlin/kotlinx.serialization/issues/2719
-keepclassmembers public class **$$serializer {
    private ** descriptor;
}
# If a companion has the serializer function, keep the companion field on the original type so that
# the reflective lookup succeeds.
-if class **.*$Companion {
  kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class <1>.<2> {
  <1>.<2>$Companion Companion;
}

-optimizations !method/specialization/parametertype

# Coil
-keep class coil3.network.ktor3.internal.KtorNetworkFetcherServiceLoaderTarget
-keep class okio.** { *; }

# Compose
-keep,allowshrinking,allowobfuscation class androidx.compose.runtime.* { *; }

# We don't use these classes
-dontwarn kotlinx.datetime.**

# Okhttp
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
# May be used with robolectric or deliberate use of Bouncy Castle on Android
-dontwarn okhttp3.internal.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
