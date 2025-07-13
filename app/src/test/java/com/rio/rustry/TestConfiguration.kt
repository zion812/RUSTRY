package com.rio.rustry

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Test configuration and utilities for the Rooster Platform
 * 
 * Provides centralized test configuration including:
 * - Coroutine test setup
 * - Mock configuration
 * - Test data management
 * - Performance testing utilities
 */

/**
 * JUnit 5 extension for coroutine testing
 */
@ExperimentalCoroutinesApi
class CoroutineTestExtension : BeforeEachCallback, AfterEachCallback {
    
    private val testDispatcher = TestCoroutineDispatcher()
    
    override fun beforeEach(context: ExtensionContext?) {
        Dispatchers.setMain(testDispatcher)
    }
    
    override fun afterEach(context: ExtensionContext?) {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
}

/**
 * Test configuration constants
 */
object TestConfig {
    
    // Test timeouts
    const val DEFAULT_TIMEOUT = 5000L // 5 seconds
    const val NETWORK_TIMEOUT = 10000L // 10 seconds
    const val DATABASE_TIMEOUT = 3000L // 3 seconds
    
    // Test data limits
    const val MAX_TEST_FOWLS = 100
    const val MAX_TEST_USERS = 50
    const val MAX_TEST_HEALTH_RECORDS = 200
    const val MAX_TEST_PAYMENTS = 75
    const val MAX_TEST_TRANSFERS = 50
    
    // Test validation rules
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_PASSWORD_LENGTH = 128
    const val MIN_NAME_LENGTH = 2
    const val MAX_NAME_LENGTH = 50
    const val MIN_FOWL_PRICE = 0.0
    const val MAX_FOWL_PRICE = 99999.99
    
    // Test coverage targets
    const val MIN_CODE_COVERAGE = 80.0
    const val MIN_BRANCH_COVERAGE = 75.0
    const val MIN_LINE_COVERAGE = 85.0
}

/**
 * Test validation utilities
 */
object TestValidation {
    
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && 
               email.contains("@") && 
               email.contains(".") &&
               email.length >= 5
    }
    
    fun isValidPassword(password: String): Boolean {
        return password.isNotBlank() && 
               password.length >= TestConfig.MIN_PASSWORD_LENGTH &&
               password.length <= TestConfig.MAX_PASSWORD_LENGTH
    }
    
    fun isValidName(name: String): Boolean {
        return name.isNotBlank() && 
               name.length >= TestConfig.MIN_NAME_LENGTH &&
               name.length <= TestConfig.MAX_NAME_LENGTH
    }
    
    fun isValidPrice(price: Double): Boolean {
        return price >= TestConfig.MIN_FOWL_PRICE && 
               price <= TestConfig.MAX_FOWL_PRICE
    }
    
    fun isValidId(id: String): Boolean {
        return id.isNotBlank() && id.length >= 3
    }
}

/**
 * Test performance utilities
 */
object TestPerformance {
    
    fun measureExecutionTime(operation: () -> Unit): Long {
        val startTime = System.currentTimeMillis()
        operation()
        return System.currentTimeMillis() - startTime
    }
    
    suspend fun measureSuspendExecutionTime(operation: suspend () -> Unit): Long {
        val startTime = System.currentTimeMillis()
        operation()
        return System.currentTimeMillis() - startTime
    }
}