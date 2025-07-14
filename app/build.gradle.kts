plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    // Temporarily disable Hilt for release build
    // alias(libs.plugins.hilt.android)
    // kotlin("kapt")
}

android {
    namespace = "com.rio.rustry"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rio.rustry"
        minSdk = 23
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.0-rc1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Performance optimizations
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // Build config fields for optimization
        buildConfigField("boolean", "ENABLE_LOGGING", "false")
        buildConfigField("boolean", "ENABLE_ANALYTICS", "true")
        buildConfigField("int", "NETWORK_TIMEOUT", "30000")
        buildConfigField("int", "IMAGE_CACHE_SIZE", "50")
    }

    buildTypes {
        release {
            isMinifyEnabled = false  // Temporarily disabled for testing
            isShrinkResources = false
            isDebuggable = false
            // proguardFiles(
            //     getDefaultProguardFile("proguard-android-optimize.txt"),
            //     "proguard-rules.pro"
            // )
            
            // Performance optimizations for release
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
            buildConfigField("boolean", "ENABLE_ANALYTICS", "true")
            
            // Signing config for release builds
            signingConfig = signingConfigs.getByName("debug") // Replace with actual signing config
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            buildConfigField("boolean", "ENABLE_ANALYTICS", "false")
        }
        create("benchmark") {
            initWith(getByName("release"))
            matchingFallbacks.add("release")
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles("benchmark-rules.pro")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn"
        )
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"  // Compatible with Kotlin 1.9.22
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/gradle/incremental.annotation.processors"
        }
    }
    
    // Lint optimizations
    lint {
        checkReleaseBuilds = false
        abortOnError = false
        disable += setOf("MissingTranslation", "ExtraTranslation")
    }
    
    // Test options
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

// kapt {
//     correctErrorTypes = true
// }

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.core:core-splashscreen:1.0.1")
    
    // Compose BOM and UI - Use BOM for consistent versioning
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")
    
    // Firebase (optimized)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-config-ktx")  // For feature flags
    implementation(libs.firebase.crashlytics)
    
    // Coroutines (latest versions)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.3")
    
    // ViewModel and Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.8.7")
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
    
    // Image loading (optimized)
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-gif:2.7.0")
    implementation("io.coil-kt:coil-svg:2.7.0")
    
    // Date picker
    implementation("io.github.vanpra.compose-material-dialogs:datetime:0.9.0")
    
    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")
    
    // Camera and Image Processing
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("androidx.exifinterface:exifinterface:1.3.7")
    
    // Room for offline persistence (optimized)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-paging:2.6.1")
    // Room annotation processor (required for Room to generate code) - TEMPORARILY DISABLED
    // kapt("androidx.room:room-compiler:2.6.1")
    
    // WorkManager for sync
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    
    // Google Pay integration
    implementation("com.google.android.gms:play-services-wallet:19.2.1")
    
    // Dependency Injection (Koin)
    implementation("io.insert-koin:koin-android:3.5.6")
    implementation("io.insert-koin:koin-androidx-compose:3.5.6")
    implementation("io.insert-koin:koin-androidx-workmanager:3.5.6")
    
    // Add explicit JavaPoet dependency for compatibility (if still needed)
    implementation("com.squareup:javapoet:1.13.0")
    
    // Paging for large datasets
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation("androidx.paging:paging-compose:3.2.1")
    
    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Network optimization
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Performance monitoring
    implementation("androidx.tracing:tracing:1.2.0")
    implementation("androidx.benchmark:benchmark-macro-junit4:1.2.2")
    
    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // QR Code generation
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    
    // Desugaring for Java 8+ APIs
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    
    // Testing dependencies
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("com.google.truth:truth:1.1.4")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("androidx.room:room-testing:2.6.1")
    
    // Android testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation("androidx.navigation:navigation-testing:2.8.3")
    androidTestImplementation("androidx.work:work-testing:2.9.0")
    // androidTestImplementation("com.google.dagger:hilt-android-testing:2.51.1")
    // kaptAndroidTest("com.google.dagger:hilt-compiler:2.51.1")
    
    // Debug dependencies
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
    
    // Additional UI components
    implementation("com.google.accompanist:accompanist-swiperefresh:0.32.0")
}
