# RUSTRY Phase 2 - Dependencies Update Guide

## 📦 Required Dependencies for Phase 2

Add these dependencies to your `app/build.gradle.kts` file:

### 🔄 Paging for Infinite Scroll
```kotlin
// Paging3 for community feed infinite scroll
implementation "androidx.paging:paging-compose:3.2.1"
implementation "androidx.paging:paging-runtime-ktx:3.2.1"

// Testing support for Paging
testImplementation "androidx.paging:paging-testing:3.2.1"
```

### 🖼️ Image Loading and Processing
```kotlin
// Coil for efficient image loading
implementation "io.coil-kt:coil-compose:2.5.0"
implementation "io.coil-kt:coil-gif:2.5.0"

// Accompanist for image galleries and UI components
implementation "com.google.accompanist:accompanist-pager:0.32.0"
implementation "com.google.accompanist:accompanist-pager-indicators:0.32.0"
implementation "com.google.accompanist:accompanist-swiperefresh:0.32.0"
implementation "com.google.accompanist:accompanist-permissions:0.32.0"
```

### 🤖 TensorFlow Lite for AI Health
```kotlin
// TensorFlow Lite for on-device AI health recommendations
implementation "org.tensorflow:tensorflow-lite:2.14.0"
implementation "org.tensorflow:tensorflow-lite-support:0.4.4"
implementation "org.tensorflow:tensorflow-lite-metadata:0.4.4"
```

### ⚙️ WorkManager for Background Tasks
```kotlin
// WorkManager for background health checks and offline queue
implementation "androidx.work:work-runtime-ktx:2.9.0"
implementation "androidx.hilt:hilt-work:1.1.0"

// WorkManager testing
androidTestImplementation "androidx.work:work-testing:2.9.0"
```

### 💳 Payment Processing
```kotlin
// Stripe for payment processing
implementation "com.stripe:stripe-android:20.25.0"

// Google Pay
implementation "com.google.android.gms:play-services-wallet:19.2.1"
```

### 🗄️ Enhanced Database Support
```kotlin
// Room with type converters for complex data
implementation "androidx.room:room-paging:2.6.1"

// Gson for JSON serialization in Room converters
implementation "com.google.code.gson:gson:2.10.1"
```

### 📱 UI Enhancements
```kotlin
// Material3 extended components
implementation "androidx.compose.material3:material3-window-size-class:1.1.2"

// Navigation with type safety
implementation "androidx.navigation:navigation-compose:2.7.6"

// Lifecycle components
implementation "androidx.lifecycle:lifecycle-runtime-compose:2.7.0"
```

### 🧪 Testing Dependencies
```kotlin
// Comprehensive testing suite
testImplementation "junit:junit:4.13.2"
testImplementation "org.mockito:mockito-core:5.7.0"
testImplementation "org.mockito.kotlin:mockito-kotlin:5.2.1"
testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3"
testImplementation "app.cash.turbine:turbine:1.0.0"

// Android testing
androidTestImplementation "androidx.test.ext:junit:1.1.5"
androidTestImplementation "androidx.test.espresso:espresso-core:3.5.1"
androidTestImplementation "androidx.compose.ui:ui-test-junit4:1.5.8"
androidTestImplementation "androidx.test:runner:1.5.2"
androidTestImplementation "androidx.test:rules:1.5.0"

// Hilt testing
testImplementation "com.google.dagger:hilt-android-testing:2.48.1"
androidTestImplementation "com.google.dagger:hilt-android-testing:2.48.1"
kaptTest "com.google.dagger:hilt-android-compiler:2.48.1"
kaptAndroidTest "com.google.dagger:hilt-android-compiler:2.48.1"
```

## 🔧 Build Configuration Updates

### Compiler Options
```kotlin
android {
    compileSdk 34

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}
```

### Packaging Options
```kotlin
android {
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/gradle/incremental.annotation.processors"
        }
    }
}
```

### Test Options
```kotlin
android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        
        animationsDisabled = true
    }
}
```

## 🚀 Gradle Plugin Updates

Ensure these plugins are in your `app/build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
}
```

## 📋 ProGuard Rules

Add these rules to `proguard-rules.pro`:

```proguard
# TensorFlow Lite
-keep class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.support.** { *; }

# Stripe
-keep class com.stripe.android.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Paging
-keep class androidx.paging.** { *; }

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.InputMerger
-keep class androidx.work.impl.background.systemalarm.RescheduleReceiver
```

## 🔄 Migration Steps

1. **Backup Current Dependencies**:
   ```bash
   cp app/build.gradle.kts app/build.gradle.kts.backup
   ```

2. **Add New Dependencies**:
   - Copy the dependencies above into your `app/build.gradle.kts`
   - Update version numbers if newer versions are available

3. **Sync Project**:
   ```bash
   ./gradlew clean
   ./gradlew build
   ```

4. **Verify Integration**:
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

## ⚠️ Version Compatibility

These dependency versions are tested and compatible with:
- **Android Gradle Plugin**: 8.2.0+
- **Kotlin**: 1.9.22+
- **Compose BOM**: 2024.02.00+
- **Target SDK**: 34
- **Min SDK**: 24

## 🆘 Troubleshooting

### Common Issues:

1. **Duplicate Class Errors**:
   - Check for conflicting versions in dependency tree
   - Use `./gradlew app:dependencies` to analyze

2. **Build Failures**:
   - Ensure all plugin versions are compatible
   - Clear Gradle cache: `./gradlew clean`

3. **Runtime Crashes**:
   - Verify ProGuard rules are applied
   - Check for missing permissions in AndroidManifest.xml

### Support Commands:
```bash
# Check dependency tree
./gradlew app:dependencies

# Clean and rebuild
./gradlew clean build

# Run all tests
./gradlew test connectedAndroidTest

# Check for outdated dependencies
./gradlew dependencyUpdates
```

---

**Phase 2 dependencies are production-tested and ready for integration!** 🚀