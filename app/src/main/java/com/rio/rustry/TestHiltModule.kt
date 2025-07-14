package com.rio.rustry

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Test module to verify Hilt is working
 */
@Module
@InstallIn(SingletonComponent::class)
object TestHiltModule {
    
    @Provides
    @Singleton
    fun provideTestString(): String = "Hilt is working!"
}