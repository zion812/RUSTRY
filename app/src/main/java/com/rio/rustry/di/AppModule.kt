package com.rio.rustry.di

import android.content.Context
import com.rio.rustry.utils.*

/**
 * Manual dependency injection container
 * Simplified approach without Hilt for now
 */
object AppModule {
    
    fun provideMemoryManager(context: Context): MemoryManager = MemoryManager(context)
    
    fun provideSecurityManager(context: Context): SecurityManager = SecurityManager(context)
    
    fun provideNetworkManager(context: Context): NetworkManager = NetworkManager(context)
    
    fun provideDatabaseOptimizer(): DatabaseOptimizer = DatabaseOptimizer()
    
    fun provideOptimizedImageLoader(context: Context): OptimizedImageLoader = OptimizedImageLoader(context)
}