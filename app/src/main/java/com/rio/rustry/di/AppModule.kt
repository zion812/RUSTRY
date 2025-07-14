package com.rio.rustry.di

import android.content.Context
import androidx.room.Room
import com.rio.rustry.data.local.FowlDao
import com.rio.rustry.data.local.RustryDatabase
import com.rio.rustry.utils.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt dependency injection module
 * Provides singleton instances of core utilities and database
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    // Core utilities
    @Provides
    @Singleton
    fun provideMemoryManager(@ApplicationContext context: Context): MemoryManager = 
        MemoryManager(context)
    
    @Provides
    @Singleton
    fun provideSecurityManager(@ApplicationContext context: Context): SecurityManager = 
        SecurityManager(context)
    
    @Provides
    @Singleton
    fun provideNetworkManager(@ApplicationContext context: Context): NetworkManager = 
        NetworkManager(context)
    
    @Provides
    @Singleton
    fun provideOptimizedImageLoader(@ApplicationContext context: Context): OptimizedImageLoader = 
        OptimizedImageLoader(context)
    
    // Database
    @Provides
    @Singleton
    fun provideRustryDatabase(@ApplicationContext context: Context): RustryDatabase =
        Room.databaseBuilder(
            context,
            RustryDatabase::class.java,
            RustryDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration() // For development - remove in production
        .build()
    
    @Provides
    fun provideFowlDao(database: RustryDatabase): FowlDao = 
        database.fowlDao()
}