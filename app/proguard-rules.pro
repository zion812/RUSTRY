# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ================================================================================================
# ROOSTER PLATFORM - OPTIMIZED PROGUARD CONFIGURATION
# ================================================================================================

# Keep line numbers for better crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations for runtime reflection
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeVisibleTypeAnnotations
-keepattributes Signature,InnerClasses,EnclosingMethod

# ================================================================================================
# KOTLIN OPTIMIZATIONS
# ================================================================================================

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# ================================================================================================
# FIREBASE OPTIMIZATIONS
# ================================================================================================

# Firebase Authentication
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.android.gms.internal.firebase-auth-api.** { *; }

# Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firestore.v1.** { *; }
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.PropertyName <fields>;
}

# Firebase Storage
-keep class com.google.firebase.storage.** { *; }

# Firebase Analytics & Crashlytics
-keep class com.google.firebase.analytics.** { *; }
-keep class com.google.firebase.crashlytics.** { *; }

# ================================================================================================
# JETPACK COMPOSE OPTIMIZATIONS
# ================================================================================================

# Compose runtime
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material3.** { *; }

# Keep Composable functions
-keep @androidx.compose.runtime.Composable class * { *; }
-keep class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Navigation Compose
-keep class androidx.navigation.** { *; }

# ================================================================================================
# HILT DEPENDENCY INJECTION
# ================================================================================================

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent
-keep class **_HiltModules { *; }
-keep class **_HiltModules$* { *; }

# Keep Hilt generated classes
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ================================================================================================
# ROOM DATABASE OPTIMIZATIONS
# ================================================================================================

# Room
-keep class androidx.room.** { *; }
-keep class androidx.sqlite.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# ================================================================================================
# ROOSTER PLATFORM SPECIFIC RULES
# ================================================================================================

# Keep all data models - CRITICAL FOR RELEASE
-keep class com.rio.rustry.data.models.** { *; }
-keep class com.rio.rustry.data.model.** { *; }

# Keep repository interfaces and implementations
-keep class com.rio.rustry.data.repository.** { *; }

# Keep ViewModels
-keep class com.rio.rustry.presentation.viewmodel.** { *; }

# Keep utility classes
-keep class com.rio.rustry.utils.** { *; }

# Keep navigation classes
-keep class com.rio.rustry.navigation.** { *; }

# Keep workers
-keep class com.rio.rustry.workers.** { *; }

# ================================================================================================
# THIRD-PARTY LIBRARY OPTIMIZATIONS
# ================================================================================================

# Coil image loading
-keep class coil.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Google Pay
-keep class com.google.android.gms.wallet.** { *; }
-keep class com.google.android.gms.common.** { *; }

# Material Dialogs
-keep class com.vanpra.composematerialdialogs.** { *; }

# Accompanist
-keep class com.google.accompanist.** { *; }

# ================================================================================================
# PERFORMANCE OPTIMIZATIONS
# ================================================================================================

# Optimize method calls
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Remove debug code
-assumenosideeffects class com.rio.rustry.BuildConfig {
    public static final boolean DEBUG return false;
}

# ================================================================================================
# SECURITY OPTIMIZATIONS
# ================================================================================================

# Obfuscate class names but keep important ones readable for debugging
-keepnames class com.rio.rustry.MainActivity
-keepnames class com.rio.rustry.RoosterApplication

# Remove unused resources
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ================================================================================================
# REFLECTION AND SERIALIZATION
# ================================================================================================

# Keep classes that use reflection
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ================================================================================================
# ERROR SUPPRESSION
# ================================================================================================

# Suppress warnings for known issues
-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn org.slf4j.**
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn kotlin.jvm.internal.**

# ================================================================================================
# DEBUGGING SUPPORT
# ================================================================================================

# Keep source file names and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable

# Keep parameter names for better debugging
-keepparameternames

# Print configuration for debugging
-printconfiguration build/outputs/mapping/configuration.txt
-printmapping build/outputs/mapping/mapping.txt
-printusage build/outputs/mapping/usage.txt
-printseeds build/outputs/mapping/seeds.txt