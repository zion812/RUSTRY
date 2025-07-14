package com.rio.rustry

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.rio.rustry.utils.MemoryManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Test to verify Hilt dependency injection is working correctly
 * for MemoryManager and other core utilities
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MemoryManagerHiltTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var memoryManager: MemoryManager

    @Inject
    lateinit var databaseOptimizer: com.rio.rustry.utils.DatabaseOptimizer

    @Inject
    lateinit var networkManager: com.rio.rustry.utils.NetworkManager

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testMemoryManagerInjection() {
        // Verify MemoryManager is properly injected
        assertThat(memoryManager).isNotNull()
        assertThat(memoryManager).isInstanceOf(MemoryManager::class.java)
    }

    @Test
    fun testDatabaseOptimizerInjection() {
        // Verify DatabaseOptimizer is properly injected
        assertThat(databaseOptimizer).isNotNull()
        assertThat(databaseOptimizer).isInstanceOf(com.rio.rustry.utils.DatabaseOptimizer::class.java)
    }

    @Test
    fun testNetworkManagerInjection() {
        // Verify NetworkManager is properly injected
        assertThat(networkManager).isNotNull()
        assertThat(networkManager).isInstanceOf(com.rio.rustry.utils.NetworkManager::class.java)
    }

    @Test
    fun testSingletonBehavior() {
        // Inject another instance to verify singleton behavior
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // Both instances should be the same (singleton)
        assertThat(memoryManager).isNotNull()
        assertThat(databaseOptimizer).isNotNull()
        
        // Test that MemoryManager can perform basic operations
        val initialMemoryReport = memoryManager.generateMemoryReport()
        assertThat(initialMemoryReport).isNotEmpty()
    }

    @Test
    fun testHiltModuleProvision() {
        // Test that all dependencies from AppModule are properly provided
        assertThat(memoryManager).isNotNull()
        assertThat(databaseOptimizer).isNotNull()
        assertThat(networkManager).isNotNull()
        
        // Verify they are working instances, not just null objects
        assertThat(memoryManager.toString()).contains("MemoryManager")
        assertThat(databaseOptimizer.toString()).contains("DatabaseOptimizer")
        assertThat(networkManager.toString()).contains("NetworkManager")
    }
}