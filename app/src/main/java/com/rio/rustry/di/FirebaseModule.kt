package com.rio.rustry.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.functions.FirebaseFunctions
import com.rio.rustry.data.repository.FirebaseFowlRepository
import com.rio.rustry.security.NetworkSecurityManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for Firebase-centric architecture
 * 
 * Provides:
 * - Firebase services with offline persistence
 * - Repository implementations
 * - Network security configuration
 * - Analytics and monitoring
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    
    /**
     * Provides Firebase Authentication instance
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    
    /**
     * Provides Firestore instance with offline persistence enabled
     */
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        
        // Configure Firestore settings for optimal offline performance
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        
        firestore.firestoreSettings = settings
        
        return firestore
    }
    
    /**
     * Provides Realtime Database reference with offline persistence
     */
    @Provides
    @Singleton
    fun provideRealtimeDatabase(): DatabaseReference {
        val database = FirebaseDatabase.getInstance()
        
        // Enable offline persistence for Realtime Database
        database.setPersistenceEnabled(true)
        
        // Set cache size for better performance
        database.setPersistenceCacheSizeBytes(10 * 1024 * 1024) // 10MB
        
        return database.reference
    }
    
    /**
     * Provides Firebase Storage instance
     */
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
    
    /**
     * Provides Firebase Analytics instance
     */
    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }
    
    /**
     * Provides Firebase Crashlytics instance
     */
    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
    
    /**
     * Provides Firebase Messaging instance
     */
    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()
    
    /**
     * Provides Firebase Functions instance
     */
    @Provides
    @Singleton
    fun provideFirebaseFunctions(): FirebaseFunctions = FirebaseFunctions.getInstance()
    
    /**
     * Provides Network Security Manager for SSL pinning
     */
    @Provides
    @Singleton
    fun provideNetworkSecurityManager(@ApplicationContext context: Context): NetworkSecurityManager {
        return NetworkSecurityManager(context)
    }
    
    /**
     * Provides Firebase Fowl Repository with real-time sync
     */
    @Provides
    @Singleton
    fun provideFirebaseFowlRepository(
        firestore: FirebaseFirestore,
        realtimeDb: DatabaseReference
    ): FirebaseFowlRepository {
        return FirebaseFowlRepository(firestore, realtimeDb)
    }
}

/**
 * Alternative module using Koin for dependency injection
 * (Since Hilt is temporarily disabled in the build)
 */
object KoinFirebaseModule {
    
    fun getFirebaseModules() = listOf(
        org.koin.dsl.module {
            
            // Firebase Auth
            single { FirebaseAuth.getInstance() }
            
            // Firestore with offline persistence
            single {
                val firestore = FirebaseFirestore.getInstance()
                val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build()
                firestore.firestoreSettings = settings
                firestore
            }
            
            // Realtime Database with offline persistence
            single {
                val database = FirebaseDatabase.getInstance()
                database.setPersistenceEnabled(true)
                database.setPersistenceCacheSizeBytes(10 * 1024 * 1024) // 10MB
                database.reference
            }
            
            // Firebase Storage
            single { FirebaseStorage.getInstance() }
            
            // Firebase Analytics
            single { FirebaseAnalytics.getInstance(get()) }
            
            // Firebase Crashlytics
            single { FirebaseCrashlytics.getInstance() }
            
            // Firebase Messaging
            single { FirebaseMessaging.getInstance() }
            
            // Firebase Functions
            single { FirebaseFunctions.getInstance() }
            
            // Network Security Manager
            single { NetworkSecurityManager(get()) }
            
            // Firebase Fowl Repository
            single { FirebaseFowlRepository(get(), get()) }
        }
    )
}