// generated/phase1/app/src/main/java/com/rio/rustry/di/Phase1Module.kt
package com.rio.rustry.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.rio.rustry.auth.GdprConsentManager
import com.rio.rustry.auth.RoleManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt dependency injection module for Phase 1 components
 * Provides singleton instances of auth and navigation related services
 */
@Module
@InstallIn(SingletonComponent::class)
object Phase1Module {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    
    @Provides
    @Singleton
    fun provideFirebaseFunctions(): FirebaseFunctions = FirebaseFunctions.getInstance()
    
    @Provides
    @Singleton
    fun provideRoleManager(
        @ApplicationContext context: Context,
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): RoleManager = RoleManager(context, auth, firestore)
    
    @Provides
    @Singleton
    fun provideGdprConsentManager(
        @ApplicationContext context: Context,
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        functions: FirebaseFunctions
    ): GdprConsentManager = GdprConsentManager(context, auth, firestore, functions)
}