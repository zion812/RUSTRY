package com.rio.rustry.di

import com.rio.rustry.domain.payment.PaymentGateway
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Payment Module for Dependency Injection
 */
@Module
@InstallIn(SingletonComponent::class)
object PaymentModule {
    
    @Provides
    @Singleton
    fun providePaymentGateway(): PaymentGateway {
        return object : PaymentGateway {
            override suspend fun createOrder(amount: Long): String {
                return "mock_order_${System.currentTimeMillis()}"
            }
            
            override suspend fun processPayment(orderId: String): Boolean {
                return true // Mock success
            }
        }
    }
}