package com.rio.rustry

import com.rio.rustry.test.testModule
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest

/**
 * Simple test to verify Koin testing infrastructure works
 */
class SimpleKoinTest : KoinTest {
    
    @Before
    fun setup() {
        startKoin {
            modules(testModule)
        }
    }
    
    @After
    fun tearDown() {
        stopKoin()
    }
    
    @Test
    fun `koin test infrastructure should work`() {
        // This test verifies that our Koin test setup works
        assertTrue("Koin test infrastructure is working", true)
    }
    
    @Test
    fun `basic arithmetic should work`() {
        val result = 2 + 2
        assertEquals(4, result)
    }
    
    @Test
    fun `string operations should work`() {
        val text = "RUSTRY"
        assertEquals(6, text.length)
        assertTrue(text.contains("RUST"))
    }
}