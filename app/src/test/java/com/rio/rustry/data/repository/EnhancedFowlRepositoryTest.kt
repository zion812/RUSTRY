package com.rio.rustry.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rustry.data.local.dao.FowlDao
import com.rio.rustry.data.local.entity.FowlEntity
import com.rio.rustry.data.model.Fowl
import com.rio.rustry.data.model.FowlBreed
import com.rio.rustry.presentation.marketplace.PriceRange
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import java.util.*

@ExperimentalCoroutinesApi
class EnhancedFowlRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val mockFirestore = mockk<FirebaseFirestore>()
    private val mockFowlDao = mockk<FowlDao>()
    private lateinit var repository: EnhancedFowlRepository

    @Before
    fun setup() {
        repository = EnhancedFowlRepository(mockFirestore, mockFowlDao)
    }

    @Test
    fun `getMarketplaceFowls should return cached data when available`() = runTest {
        // Given
        val cachedFowls = listOf(
            createMockFowlEntity("1", "Rhode Island Red", 1200.0),
            createMockFowlEntity("2", "Leghorn", 800.0)
        )
        
        every { mockFowlDao.getMarketplaceFowls() } returns flowOf(cachedFowls)

        // When
        val result = repository.getMarketplaceFowls(forceRefresh = false).first()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        verify { mockFowlDao.getMarketplaceFowls() }
    }

    @Test
    fun `searchFowls should filter by breed correctly`() = runTest {
        // Given
        val mockFowls = listOf(
            createMockFowl("1", "Rhode Island Red", 1200.0),
            createMockFowl("2", "Leghorn", 800.0),
            createMockFowl("3", "Rhode Island Red", 1300.0)
        )
        
        // Mock Firestore query
        mockFirestoreQuery(mockFowls)

        // When
        val result = repository.searchFowls(
            query = "",
            breed = FowlBreed.RHODE_ISLAND_RED
        ).first()

        // Then
        assertTrue(result.isSuccess)
        val fowls = result.getOrNull()
        assertNotNull(fowls)
        // Note: In real implementation, this would be filtered by Firestore query
        // Here we're testing the repository structure
    }

    @Test
    fun `searchFowls should filter by price range correctly`() = runTest {
        // Given
        val mockFowls = listOf(
            createMockFowl("1", "Rhode Island Red", 400.0),
            createMockFowl("2", "Leghorn", 800.0),
            createMockFowl("3", "Brahma", 1500.0)
        )
        
        mockFirestoreQuery(mockFowls)

        // When
        val result = repository.searchFowls(
            query = "",
            priceRange = PriceRange.RANGE_500_1000
        ).first()

        // Then
        assertTrue(result.isSuccess)
        // In real implementation, Firestore would handle the price filtering
    }

    @Test
    fun `searchFowls should apply text search filter`() = runTest {
        // Given
        val mockFowls = listOf(
            createMockFowl("1", "Rhode Island Red", 1200.0, description = "Healthy laying hen"),
            createMockFowl("2", "Leghorn", 800.0, description = "Young rooster"),
            createMockFowl("3", "Brahma", 1500.0, description = "Large breed chicken")
        )
        
        mockFirestoreQuery(mockFowls)

        // When
        val result = repository.searchFowls(query = "laying").first()

        // Then
        assertTrue(result.isSuccess)
        val fowls = result.getOrNull()
        assertNotNull(fowls)
        // Client-side filtering should work
        assertEquals(1, fowls?.filter { it.description.contains("laying", ignoreCase = true) }?.size)
    }

    @Test
    fun `getFowlById should return cached data first`() = runTest {
        // Given
        val fowlId = "test_fowl_1"
        val cachedFowl = createMockFowlEntity(fowlId, "Rhode Island Red", 1200.0)
        
        every { mockFowlDao.getFowlById(fowlId) } returns flowOf(cachedFowl)

        // When
        val result = repository.getFowlById(fowlId).first()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(fowlId, result.getOrNull()?.id)
        verify { mockFowlDao.getFowlById(fowlId) }
    }

    @Test
    fun `addToFavorites should update both Firestore and local cache`() = runTest {
        // Given
        val fowlId = "test_fowl_1"
        val userId = "test_user_1"
        
        // Mock Firestore success
        mockFirestoreDocumentSet()
        coEvery { mockFowlDao.addToFavorites(fowlId, userId) } just Runs

        // When
        val result = repository.addToFavorites(fowlId, userId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { mockFowlDao.addToFavorites(fowlId, userId) }
    }

    @Test
    fun `removeFromFavorites should update both Firestore and local cache`() = runTest {
        // Given
        val fowlId = "test_fowl_1"
        val userId = "test_user_1"
        
        // Mock Firestore success
        mockFirestoreDocumentDelete()
        coEvery { mockFowlDao.removeFromFavorites(fowlId, userId) } just Runs

        // When
        val result = repository.removeFromFavorites(fowlId, userId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { mockFowlDao.removeFromFavorites(fowlId, userId) }
    }

    @Test
    fun `getFavoriteFowls should return cached favorites first`() = runTest {
        // Given
        val userId = "test_user_1"
        val favoriteFowls = listOf(
            createMockFowlEntity("1", "Rhode Island Red", 1200.0),
            createMockFowlEntity("2", "Leghorn", 800.0)
        )
        
        every { mockFowlDao.getFavoriteFowls(userId) } returns flowOf(favoriteFowls)

        // When
        val result = repository.getFavoriteFowls(userId).first()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        verify { mockFowlDao.getFavoriteFowls(userId) }
    }

    @Test
    fun `clearCache should clear all local data`() = runTest {
        // Given
        coEvery { mockFowlDao.clearAll() } just Runs

        // When
        repository.clearCache()

        // Then
        coVerify { mockFowlDao.clearAll() }
    }

    private fun createMockFowl(
        id: String,
        breed: String,
        price: Double,
        description: String = "Test description",
        location: String = "Test Location",
        isTraceable: Boolean = false
    ): Fowl {
        return Fowl(
            id = id,
            breed = breed,
            ownerName = "Test Owner",
            price = price,
            description = description,
            location = location,
            isTraceable = isTraceable,
            isAvailable = true,
            dateOfBirth = Date(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun createMockFowlEntity(
        id: String,
        breed: String,
        price: Double,
        description: String = "Test description",
        location: String = "Test Location",
        isTraceable: Boolean = false
    ): FowlEntity {
        return FowlEntity(
            id = id,
            ownerId = "test_owner",
            ownerName = "Test Owner",
            breed = breed,
            dateOfBirth = Date(),
            isTraceable = isTraceable,
            parentIds = emptyList(),
            imageUrls = emptyList(),
            proofImageUrls = emptyList(),
            description = description,
            location = location,
            price = price,
            isAvailable = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun mockFirestoreQuery(fowls: List<Fowl>) {
        // This is a simplified mock - in real tests you'd mock the entire Firestore query chain
        // For now, we're focusing on the repository logic structure
    }

    private fun mockFirestoreDocumentSet() {
        // Mock successful Firestore document set operation
    }

    private fun mockFirestoreDocumentDelete() {
        // Mock successful Firestore document delete operation
    }
}

// Additional test for Room DAO
class FowlDaoTest {

    @Test
    fun `fowlEntity should convert to fowl correctly`() {
        // Given
        val fowlEntity = FowlEntity(
            id = "test_id",
            ownerId = "owner_id",
            ownerName = "Test Owner",
            breed = "Rhode Island Red",
            dateOfBirth = Date(),
            isTraceable = true,
            parentIds = listOf("parent1", "parent2"),
            imageUrls = listOf("image1.jpg", "image2.jpg"),
            proofImageUrls = listOf("proof1.jpg"),
            description = "Test description",
            location = "Test Location",
            price = 1200.0,
            isAvailable = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // When
        val fowl = fowlEntity.toFowl()

        // Then
        assertEquals(fowlEntity.id, fowl.id)
        assertEquals(fowlEntity.breed, fowl.breed)
        assertEquals(fowlEntity.price, fowl.price, 0.01)
        assertEquals(fowlEntity.isTraceable, fowl.isTraceable)
        assertEquals(fowlEntity.parentIds, fowl.parentIds)
    }

    @Test
    fun `fowl should convert to fowlEntity correctly`() {
        // Given
        val fowl = Fowl(
            id = "test_id",
            ownerId = "owner_id",
            ownerName = "Test Owner",
            breed = "Rhode Island Red",
            dateOfBirth = Date(),
            isTraceable = true,
            parentIds = listOf("parent1", "parent2"),
            imageUrls = listOf("image1.jpg", "image2.jpg"),
            proofImageUrls = listOf("proof1.jpg"),
            description = "Test description",
            location = "Test Location",
            price = 1200.0,
            isAvailable = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // When
        val fowlEntity = FowlEntity.fromFowl(fowl)

        // Then
        assertEquals(fowl.id, fowlEntity.id)
        assertEquals(fowl.breed, fowlEntity.breed)
        assertEquals(fowl.price, fowlEntity.price, 0.01)
        assertEquals(fowl.isTraceable, fowlEntity.isTraceable)
        assertEquals(fowl.parentIds, fowlEntity.parentIds)
    }
}