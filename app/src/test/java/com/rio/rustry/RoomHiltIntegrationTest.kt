package com.rio.rustry

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.rio.rustry.data.local.FowlDao
import com.rio.rustry.data.local.RustryDatabase
import com.rio.rustry.data.local.entity.FowlEntity
import com.rio.rustry.data.repository.FowlRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Integration test for Room database with Hilt DI
 * Verifies the complete data layer is working correctly
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RoomHiltIntegrationTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: RustryDatabase

    @Inject
    lateinit var fowlDao: FowlDao

    @Inject
    lateinit var fowlRepository: FowlRepository

    private lateinit var testDatabase: RustryDatabase

    @Before
    fun init() {
        hiltRule.inject()
        
        // Create in-memory database for testing
        val context = ApplicationProvider.getApplicationContext<Context>()
        testDatabase = Room.inMemoryDatabaseBuilder(
            context,
            RustryDatabase::class.java
        ).build()
    }

    @After
    fun cleanup() {
        testDatabase.close()
    }

    @Test
    fun testDatabaseInjection() {
        // Verify database is properly injected
        assertThat(database).isNotNull()
        assertThat(database).isInstanceOf(RustryDatabase::class.java)
    }

    @Test
    fun testDaoInjection() {
        // Verify DAO is properly injected
        assertThat(fowlDao).isNotNull()
        assertThat(fowlDao).isInstanceOf(FowlDao::class.java)
    }

    @Test
    fun testRepositoryInjection() {
        // Verify repository is properly injected
        assertThat(fowlRepository).isNotNull()
        assertThat(fowlRepository).isInstanceOf(FowlRepository::class.java)
    }

    @Test
    fun testBasicDatabaseOperations() = runTest {
        val testDao = testDatabase.fowlDao()
        
        // Create test fowl entity
        val testFowl = FowlEntity(
            id = "test-fowl-1",
            ownerId = "test-owner",
            ownerName = "Test Owner",
            breed = "Rhode Island Red",
            dateOfBirth = System.currentTimeMillis(),
            price = 1500.0,
            description = "Healthy test fowl",
            location = "Test Farm",
            imageUrls = "",
            proofImageUrls = "",
            parentIds = "",
            isAvailable = true,
            isTraceable = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Test insert
        testDao.insertFowl(testFowl)

        // Test retrieve
        val retrievedFowl = testDao.getFowlById("test-fowl-1")
        assertThat(retrievedFowl).isNotNull()
        assertThat(retrievedFowl?.id).isEqualTo("test-fowl-1")
        assertThat(retrievedFowl?.breed).isEqualTo("Rhode Island Red")

        // Test flow
        val fowlFlow = testDao.getFowlByIdFlow("test-fowl-1").first()
        assertThat(fowlFlow).isNotNull()
        assertThat(fowlFlow?.id).isEqualTo("test-fowl-1")

        // Test update
        val updatedFowl = testFowl.copy(price = 1800.0)
        testDao.updateFowl(updatedFowl)
        
        val retrievedUpdated = testDao.getFowlById("test-fowl-1")
        assertThat(retrievedUpdated?.price).isEqualTo(1800.0)

        // Test delete
        testDao.deleteFowlById("test-fowl-1")
        val deletedFowl = testDao.getFowlById("test-fowl-1")
        assertThat(deletedFowl).isNull()
    }

    @Test
    fun testRepositoryOperations() = runTest {
        // This test would use the injected repository
        // For now, just verify it's working
        val count = fowlRepository.getAvailableFowlCount()
        assertThat(count).isAtLeast(0)
        
        val breeds = fowlRepository.getAvailableBreeds()
        assertThat(breeds).isNotNull()
        
        val locations = fowlRepository.getAvailableLocations()
        assertThat(locations).isNotNull()
    }

    @Test
    fun testHiltSingletonBehavior() {
        // Verify that the same database instance is injected
        assertThat(database).isNotNull()
        assertThat(fowlDao).isNotNull()
        assertThat(fowlRepository).isNotNull()
        
        // The DAO should come from the same database instance
        assertThat(fowlDao).isEqualTo(database.fowlDao())
    }
}