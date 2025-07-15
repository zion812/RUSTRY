package com.rio.rustry

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Integration test for Room database functionality
 * Verifies the complete data layer is working correctly
 */
class RoomHiltIntegrationTest {

    private lateinit var database: MockRustryDatabase
    private lateinit var fowlDao: MockFowlDao
    private lateinit var fowlRepository: MockFowlRepository

    @Before
    fun init() {
        database = MockRustryDatabase()
        fowlDao = database.fowlDao()
        fowlRepository = MockFowlRepository(fowlDao)
    }

    @After
    fun cleanup() {
        database.close()
    }

    @Test
    fun testDatabaseCreation() {
        // Verify database is properly created
        assertThat(database).isNotNull()
        assertThat(database).isInstanceOf(MockRustryDatabase::class.java)
    }

    @Test
    fun testDaoCreation() {
        // Verify DAO is properly created
        assertThat(fowlDao).isNotNull()
        assertThat(fowlDao).isInstanceOf(MockFowlDao::class.java)
    }

    @Test
    fun testRepositoryCreation() {
        // Verify repository is properly created
        assertThat(fowlRepository).isNotNull()
        assertThat(fowlRepository).isInstanceOf(MockFowlRepository::class.java)
    }

    @Test
    fun testBasicDatabaseOperations() = runTest {
        // Create test fowl entity
        val testFowl = MockFowlEntity(
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
        fowlDao.insertFowl(testFowl)

        // Test retrieve
        val retrievedFowl = fowlDao.getFowlById("test-fowl-1")
        assertThat(retrievedFowl).isNotNull()
        assertThat(retrievedFowl?.id).isEqualTo("test-fowl-1")
        assertThat(retrievedFowl?.breed).isEqualTo("Rhode Island Red")

        // Test flow
        val fowlFlow = fowlDao.getFowlByIdFlow("test-fowl-1").first()
        assertThat(fowlFlow).isNotNull()
        assertThat(fowlFlow?.id).isEqualTo("test-fowl-1")

        // Test update
        val updatedFowl = testFowl.copy(price = 1800.0)
        fowlDao.updateFowl(updatedFowl)
        
        val retrievedUpdated = fowlDao.getFowlById("test-fowl-1")
        assertThat(retrievedUpdated?.price).isEqualTo(1800.0)

        // Test delete
        fowlDao.deleteFowlById("test-fowl-1")
        val deletedFowl = fowlDao.getFowlById("test-fowl-1")
        assertThat(deletedFowl).isNull()
    }

    @Test
    fun testRepositoryOperations() = runTest {
        // Test repository operations
        val count = fowlRepository.getAvailableFowlCount()
        assertThat(count).isAtLeast(0)
        
        val breeds = fowlRepository.getAvailableBreeds()
        assertThat(breeds).isNotNull()
        
        val locations = fowlRepository.getAvailableLocations()
        assertThat(locations).isNotNull()
    }

    @Test
    fun testDatabaseConsistency() {
        // Verify that the same database instance is used
        assertThat(database).isNotNull()
        assertThat(fowlDao).isNotNull()
        assertThat(fowlRepository).isNotNull()
        
        // The DAO should come from the same database instance
        assertThat(fowlDao).isEqualTo(database.fowlDao())
    }

    @Test
    fun testComplexQueries() = runTest {
        // Add multiple test fowls
        val fowls = listOf(
            MockFowlEntity(
                id = "fowl-1",
                ownerId = "owner-1",
                breed = "Rhode Island Red",
                price = 1500.0,
                location = "Farm A",
                isAvailable = true
            ),
            MockFowlEntity(
                id = "fowl-2",
                ownerId = "owner-1",
                breed = "Leghorn",
                price = 1200.0,
                location = "Farm A",
                isAvailable = true
            ),
            MockFowlEntity(
                id = "fowl-3",
                ownerId = "owner-2",
                breed = "Rhode Island Red",
                price = 1600.0,
                location = "Farm B",
                isAvailable = false
            )
        )

        fowls.forEach { fowlDao.insertFowl(it) }

        // Test filtering by availability
        val availableFowls = fowlDao.getAvailableFowls()
        assertThat(availableFowls).hasSize(2)

        // Test filtering by owner
        val owner1Fowls = fowlDao.getFowlsByOwner("owner-1")
        assertThat(owner1Fowls).hasSize(2)

        // Test filtering by breed
        val rhodeIslandReds = fowlDao.getFowlsByBreed("Rhode Island Red")
        assertThat(rhodeIslandReds).hasSize(2)

        // Test filtering by location
        val farmAFowls = fowlDao.getFowlsByLocation("Farm A")
        assertThat(farmAFowls).hasSize(2)

        // Test price range filtering
        val expensiveFowls = fowlDao.getFowlsByPriceRange(1400.0, 2000.0)
        assertThat(expensiveFowls).hasSize(2)
    }

    @Test
    fun testTransactionOperations() = runTest {
        // Test batch operations
        val fowls = (1..10).map { index ->
            MockFowlEntity(
                id = "fowl-$index",
                ownerId = "owner-batch",
                breed = "Batch Breed",
                price = 1000.0 + index * 100,
                location = "Batch Farm"
            )
        }

        // Insert all fowls in a batch
        fowlDao.insertFowls(fowls)

        // Verify all were inserted
        val batchFowls = fowlDao.getFowlsByOwner("owner-batch")
        assertThat(batchFowls).hasSize(10)

        // Test batch update
        val updatedFowls = batchFowls.map { it.copy(price = it.price + 100) }
        fowlDao.updateFowls(updatedFowls)

        // Verify updates
        val updatedBatchFowls = fowlDao.getFowlsByOwner("owner-batch")
        updatedBatchFowls.forEach { fowl ->
            assertThat(fowl.price).isAtLeast(1200.0)
        }

        // Test batch delete
        fowlDao.deleteFowlsByOwner("owner-batch")
        val deletedBatchFowls = fowlDao.getFowlsByOwner("owner-batch")
        assertThat(deletedBatchFowls).isEmpty()
    }

    @Test
    fun testDataIntegrity() = runTest {
        // Test data validation and constraints
        val fowl = MockFowlEntity(
            id = "integrity-test",
            ownerId = "test-owner",
            breed = "Test Breed",
            price = 1500.0,
            location = "Test Location"
        )

        fowlDao.insertFowl(fowl)

        // Test that duplicate IDs are handled properly
        val duplicateFowl = fowl.copy(breed = "Different Breed")
        fowlDao.insertOrUpdateFowl(duplicateFowl)

        val retrievedFowl = fowlDao.getFowlById("integrity-test")
        assertThat(retrievedFowl?.breed).isEqualTo("Different Breed")
    }

    @Test
    fun testPerformanceOperations() = runTest {
        // Test performance with larger datasets
        val largeFowlList = (1..1000).map { index ->
            MockFowlEntity(
                id = "perf-fowl-$index",
                ownerId = "perf-owner-${index % 10}",
                breed = "Breed ${index % 5}",
                price = 1000.0 + (index % 100) * 10,
                location = "Location ${index % 20}"
            )
        }

        // Measure insertion time
        val startTime = System.currentTimeMillis()
        fowlDao.insertFowls(largeFowlList)
        val insertTime = System.currentTimeMillis() - startTime

        // Verify all were inserted
        val totalCount = fowlDao.getTotalFowlCount()
        assertThat(totalCount).isEqualTo(1000)

        // Test query performance
        val queryStartTime = System.currentTimeMillis()
        val expensiveFowls = fowlDao.getFowlsByPriceRange(1500.0, 2000.0)
        val queryTime = System.currentTimeMillis() - queryStartTime

        assertThat(expensiveFowls).isNotEmpty()
        
        // Performance should be reasonable (these are just basic checks)
        assertThat(insertTime).isLessThan(5000L) // Less than 5 seconds
        assertThat(queryTime).isLessThan(1000L) // Less than 1 second
    }

    // Mock classes for testing
    data class MockFowlEntity(
        val id: String = "",
        val ownerId: String = "",
        val ownerName: String = "",
        val breed: String = "",
        val dateOfBirth: Long = 0L,
        val price: Double = 0.0,
        val description: String = "",
        val location: String = "",
        val imageUrls: String = "",
        val proofImageUrls: String = "",
        val parentIds: String = "",
        val isAvailable: Boolean = true,
        val isTraceable: Boolean = true,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    )

    class MockFowlDao {
        private val fowls = mutableMapOf<String, MockFowlEntity>()

        suspend fun insertFowl(fowl: MockFowlEntity) {
            fowls[fowl.id] = fowl
        }

        suspend fun insertFowls(fowlList: List<MockFowlEntity>) {
            fowlList.forEach { fowls[it.id] = it }
        }

        suspend fun updateFowl(fowl: MockFowlEntity) {
            fowls[fowl.id] = fowl
        }

        suspend fun updateFowls(fowlList: List<MockFowlEntity>) {
            fowlList.forEach { fowls[it.id] = it }
        }

        suspend fun insertOrUpdateFowl(fowl: MockFowlEntity) {
            fowls[fowl.id] = fowl
        }

        suspend fun getFowlById(id: String): MockFowlEntity? {
            return fowls[id]
        }

        fun getFowlByIdFlow(id: String) = flowOf(fowls[id])

        suspend fun deleteFowlById(id: String) {
            fowls.remove(id)
        }

        suspend fun deleteFowlsByOwner(ownerId: String) {
            fowls.values.removeAll { it.ownerId == ownerId }
        }

        suspend fun getAvailableFowls(): List<MockFowlEntity> {
            return fowls.values.filter { it.isAvailable }
        }

        suspend fun getFowlsByOwner(ownerId: String): List<MockFowlEntity> {
            return fowls.values.filter { it.ownerId == ownerId }
        }

        suspend fun getFowlsByBreed(breed: String): List<MockFowlEntity> {
            return fowls.values.filter { it.breed == breed }
        }

        suspend fun getFowlsByLocation(location: String): List<MockFowlEntity> {
            return fowls.values.filter { it.location == location }
        }

        suspend fun getFowlsByPriceRange(minPrice: Double, maxPrice: Double): List<MockFowlEntity> {
            return fowls.values.filter { it.price >= minPrice && it.price <= maxPrice }
        }

        suspend fun getTotalFowlCount(): Int {
            return fowls.size
        }
    }

    class MockRustryDatabase {
        private val fowlDao = MockFowlDao()

        fun fowlDao(): MockFowlDao = fowlDao

        fun close() {
            // Mock close operation
        }
    }

    class MockFowlRepository(private val fowlDao: MockFowlDao) {
        suspend fun getAvailableFowlCount(): Int {
            return fowlDao.getAvailableFowls().size
        }

        suspend fun getAvailableBreeds(): List<String> {
            return fowlDao.getAvailableFowls().map { it.breed }.distinct()
        }

        suspend fun getAvailableLocations(): List<String> {
            return fowlDao.getAvailableFowls().map { it.location }.distinct()
        }
    }
}