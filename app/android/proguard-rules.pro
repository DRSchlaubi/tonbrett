# Ktor
-keepclassmembers class io.ktor.** { volatile <fields>; }
-keep class io.ktor.client.engine.okhttp.OkHttpEngineContainer
-keep class io.ktor.serialization.kotlinx.json.KotlinxSerializationJsonExtensionProvider
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.atomicfu.**
-dontwarn io.netty.**
-dontwarn com.typesafe.**
-dontwarn okhttp3.**
-dontwarn io.ktor.**

# okhttp
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# kmongo
-keep class org.litote.kmongo.id.UUIDStringIdGeneratorProvider
-keepclassmembers class org.litote.kmongo.id.StringId {
    public <init>(java.lang.String);
}

# serialization
# For some reason if we don't do this, we get a VerifyError at runtime
-keep class kotlinx.serialization.* { *; }
-keep class dev.schlaubi.tonbrett.common.** { *; }

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

# protobuf
-keep class dev.schlaubi.tonbrett.app.android.shared.* { *; }
