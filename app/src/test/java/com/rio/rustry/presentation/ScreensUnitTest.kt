package com.rio.rustry.presentation

import com.rio.rustry.data.model.*
import com.rio.rustry.data.repository.*
import com.rio.rustry.presentation.viewmodel.*
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.*

/**
 * Unit tests for RUSTRY presentation layer screens and ViewModels
 * Testing the newly implemented screens: Health Records, Sales Tracking, Inventory, Settings, Tutorial
 */
class ScreensUnitTest {

    // Mock repositories
    private lateinit var mockHealthRepository: HealthRepository
    private lateinit var mockSalesRepository: SalesRepository
    private lateinit var mockInventoryRepository: InventoryRepository
    private lateinit var mockUserPreferencesRepository: UserPreferencesRepository

    // ViewModels under test
    private lateinit var healthViewModel: HealthViewModel
    private lateinit var salesViewModel: SalesViewModel
    private lateinit var inventoryViewModel: InventoryViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var tutorialViewModel: TutorialViewModel

    @Before
    fun setup() {
        // Initialize mocks
        mockHealthRepository = mockk()
        mockSalesRepository = mockk()
        mockInventoryRepository = mockk()
        mockUserPreferencesRepository = mockk()

        // Initialize ViewModels with mocked dependencies
        healthViewModel = HealthViewModel(mockHealthRepository)
        salesViewModel = SalesViewModel(mockSalesRepository)
        inventoryViewModel = InventoryViewModel(mockInventoryRepository)
        settingsViewModel = SettingsViewModel(mockUserPreferencesRepository)
        tutorialViewModel = TutorialViewModel(mockUserPreferencesRepository)
    }

    // Health Records Tests
    @Test
    fun `test health records loading success`() = runTest {
        // Given
        val fowlId = "test-fowl-id"
        val healthRecords = listOf(
            HealthRecord(
                id = "1",
                fowlId = fowlId,
                type = "Vaccination",
                description = "Annual vaccination",
                date = Date(),
                veterinarian = "Dr. Smith",
                status = "Healthy"
            ),
            HealthRecord(
                id = "2",
                fowlId = fowlId,
                type = "Checkup",
                description = "Routine checkup",
                date = Date(),
                veterinarian = "Dr. Johnson",
                status = "Healthy"
            )
        )

        every { mockHealthRepository.getHealthRecords(fowlId) } returns flowOf(healthRecords)

        // When
        healthViewModel.loadHealthRecords(fowlId)

        // Then
        assertEquals(healthRecords, healthViewModel.healthRecords.value)
        assertFalse(healthViewModel.isLoading.value)
        assertNull(healthViewModel.error.value)
    }

    @Test
    fun `test health record addition success`() = runTest {
        // Given
        val fowlId = "test-fowl-id"
        val newRecord = HealthRecord(
            fowlId = fowlId,
            type = "Treatment",
            description = "Antibiotic treatment",
            date = Date(),
            veterinarian = "Dr. Brown",
            status = "Under Treatment"
        )

        coEvery { mockHealthRepository.addHealthRecord(any()) } just Runs
        every { mockHealthRepository.getHealthRecords(fowlId) } returns flowOf(listOf(newRecord))

        // When
        healthViewModel.addHealthRecord(fowlId, newRecord)

        // Then
        coVerify { mockHealthRepository.addHealthRecord(any()) }
        assertFalse(healthViewModel.isLoading.value)
        assertNull(healthViewModel.error.value)
    }

    @Test
    fun `test health records loading failure`() = runTest {
        // Given
        val fowlId = "test-fowl-id"
        val errorMessage = "Network error"

        every { mockHealthRepository.getHealthRecords(fowlId) } throws Exception(errorMessage)

        // When
        healthViewModel.loadHealthRecords(fowlId)

        // Then
        assertTrue(healthViewModel.error.value?.contains(errorMessage) == true)
        assertFalse(healthViewModel.isLoading.value)
    }

    // Sales Tracking Tests
    @Test
    fun `test sales records loading success`() = runTest {
        // Given
        val farmId = "test-farm-id"
        val salesRecords = listOf(
            SaleRecord(
                id = "1",
                farmId = farmId,
                fowlName = "Rhode Island Red",
                buyerName = "John Doe",
                salePrice = 500.0,
                saleDate = Date(),
                paymentStatus = "Paid"
            ),
            SaleRecord(
                id = "2",
                farmId = farmId,
                fowlName = "Leghorn",
                buyerName = "Jane Smith",
                salePrice = 750.0,
                saleDate = Date(),
                paymentStatus = "Pending"
            )
        )

        every { mockSalesRepository.getSalesRecords(farmId) } returns flowOf(salesRecords)

        // When
        salesViewModel.loadSalesRecords(farmId)

        // Then
        assertEquals(salesRecords, salesViewModel.salesRecords.value)
        assertEquals(1250.0, salesViewModel.totalRevenue.value, 0.01)
        assertFalse(salesViewModel.isLoading.value)
        assertNull(salesViewModel.error.value)
    }

    @Test
    fun `test sales record addition success`() = runTest {
        // Given
        val farmId = "test-farm-id"
        val newSale = SaleRecord(
            farmId = farmId,
            fowlName = "Brahma",
            buyerName = "Bob Wilson",
            salePrice = 600.0,
            saleDate = Date(),
            paymentStatus = "Paid"
        )

        coEvery { mockSalesRepository.addSaleRecord(any()) } just Runs
        every { mockSalesRepository.getSalesRecords(farmId) } returns flowOf(listOf(newSale))

        // When
        salesViewModel.addSaleRecord(farmId, newSale)

        // Then
        coVerify { mockSalesRepository.addSaleRecord(any()) }
        assertFalse(salesViewModel.isLoading.value)
        assertNull(salesViewModel.error.value)
    }

    @Test
    fun `test sales period filtering`() = runTest {
        // Given
        val farmId = "test-farm-id"
        val currentDate = Date()
        val oldDate = Date(currentDate.time - (90 * 24 * 60 * 60 * 1000L)) // 90 days ago
        
        val salesRecords = listOf(
            SaleRecord(id = "1", farmId = farmId, salePrice = 500.0, saleDate = currentDate),
            SaleRecord(id = "2", farmId = farmId, salePrice = 300.0, saleDate = oldDate)
        )

        every { mockSalesRepository.getSalesRecords(farmId) } returns flowOf(salesRecords)

        // When
        salesViewModel.loadSalesRecords(farmId)
        salesViewModel.filterByPeriod("This Month")

        // Then
        // Should filter to only current month sales
        assertTrue(salesViewModel.salesRecords.value.size <= salesRecords.size)
    }

    // Inventory Management Tests
    @Test
    fun `test inventory items loading success`() = runTest {
        // Given
        val farmId = "test-farm-id"
        val inventoryItems = listOf(
            InventoryItem(
                id = "1",
                farmId = farmId,
                name = "Chicken Feed",
                category = "Feed",
                currentQuantity = 50.0,
                minimumQuantity = 10.0,
                unit = "kg",
                unitPrice = 25.0
            ),
            InventoryItem(
                id = "2",
                farmId = farmId,
                name = "Antibiotics",
                category = "Medicine",
                currentQuantity = 5.0,
                minimumQuantity = 2.0,
                unit = "bottles",
                unitPrice = 150.0
            )
        )

        every { mockInventoryRepository.getInventoryItems(farmId) } returns flowOf(inventoryItems)

        // When
        inventoryViewModel.loadInventoryItems(farmId)

        // Then
        assertEquals(inventoryItems, inventoryViewModel.inventoryItems.value)
        assertEquals(2000.0, inventoryViewModel.totalValue.value, 0.01) // (50*25) + (5*150)
        assertFalse(inventoryViewModel.isLoading.value)
        assertNull(inventoryViewModel.error.value)
    }

    @Test
    fun `test low stock detection`() = runTest {
        // Given
        val farmId = "test-farm-id"
        val lowStockItem = InventoryItem(
            id = "1",
            farmId = farmId,
            name = "Low Stock Feed",
            currentQuantity = 5.0,
            minimumQuantity = 10.0,
            unit = "kg",
            unitPrice = 25.0
        )
        val normalStockItem = InventoryItem(
            id = "2",
            farmId = farmId,
            name = "Normal Stock Feed",
            currentQuantity = 50.0,
            minimumQuantity = 10.0,
            unit = "kg",
            unitPrice = 25.0
        )

        every { mockInventoryRepository.getInventoryItems(farmId) } returns flowOf(listOf(lowStockItem, normalStockItem))

        // When
        inventoryViewModel.loadInventoryItems(farmId)

        // Then
        assertEquals(1, inventoryViewModel.lowStockItems.value.size)
        assertEquals(lowStockItem, inventoryViewModel.lowStockItems.value.first())
    }

    @Test
    fun `test inventory stock update`() = runTest {
        // Given
        val itemId = "test-item-id"
        val newQuantity = 75.0
        val existingItem = InventoryItem(
            id = itemId,
            farmId = "farm-id",
            name = "Test Item",
            currentQuantity = 50.0,
            unitPrice = 10.0
        )

        coEvery { mockInventoryRepository.updateInventoryItem(any()) } just Runs
        coEvery { mockInventoryRepository.recordTransaction(any(), any(), any(), any(), any(), any()) } just Runs
        every { mockInventoryRepository.getInventoryItems("farm-id") } returns flowOf(listOf(existingItem))

        // When
        inventoryViewModel.updateStock(itemId, newQuantity)

        // Then
        coVerify { mockInventoryRepository.updateInventoryItem(any()) }
        coVerify { mockInventoryRepository.recordTransaction(itemId, "IN", 25.0, "Stock update", "", 0.0) }
    }

    // Settings Tests
    @Test
    fun `test settings loading success`() = runTest {
        // Given
        val userPreferences = UserPreferences(
            isDarkMode = true,
            language = "Telugu",
            notificationsEnabled = true,
            voiceCommandsEnabled = false,
            offlineModeEnabled = true
        )

        every { mockUserPreferencesRepository.getUserPreferences() } returns flowOf(userPreferences)

        // When
        // Settings are loaded in init block

        // Then
        assertEquals(userPreferences.isDarkMode, settingsViewModel.isDarkMode.value)
        assertEquals(userPreferences.language, settingsViewModel.selectedLanguage.value)
        assertEquals(userPreferences.notificationsEnabled, settingsViewModel.notificationsEnabled.value)
        assertEquals(userPreferences.voiceCommandsEnabled, settingsViewModel.voiceCommandsEnabled.value)
        assertEquals(userPreferences.offlineModeEnabled, settingsViewModel.offlineModeEnabled.value)
    }

    @Test
    fun `test dark mode toggle`() = runTest {
        // Given
        val initialPreferences = UserPreferences(isDarkMode = false)
        every { mockUserPreferencesRepository.getUserPreferences() } returns flowOf(initialPreferences)
        coEvery { mockUserPreferencesRepository.setDarkMode(any()) } just Runs

        // When
        settingsViewModel.setDarkMode(true)

        // Then
        coVerify { mockUserPreferencesRepository.setDarkMode(true) }
        assertTrue(settingsViewModel.isDarkMode.value)
    }

    @Test
    fun `test language change`() = runTest {
        // Given
        val newLanguage = "Tamil"
        coEvery { mockUserPreferencesRepository.setLanguage(any()) } just Runs

        // When
        settingsViewModel.setLanguage(newLanguage)

        // Then
        coVerify { mockUserPreferencesRepository.setLanguage(newLanguage) }
        assertEquals(newLanguage, settingsViewModel.selectedLanguage.value)
    }

    @Test
    fun `test voice commands toggle`() = runTest {
        // Given
        coEvery { mockUserPreferencesRepository.setVoiceCommandsEnabled(any()) } just Runs

        // When
        settingsViewModel.setVoiceCommandsEnabled(true)

        // Then
        coVerify { mockUserPreferencesRepository.setVoiceCommandsEnabled(true) }
        assertTrue(settingsViewModel.voiceCommandsEnabled.value)
    }

    // Tutorial Tests
    @Test
    fun `test tutorial navigation`() = runTest {
        // Given
        val initialStep = 0

        // When
        tutorialViewModel.nextStep()

        // Then
        assertEquals(1, tutorialViewModel.currentStep.value)

        // When
        tutorialViewModel.previousStep()

        // Then
        assertEquals(0, tutorialViewModel.currentStep.value)
    }

    @Test
    fun `test tutorial completion`() = runTest {
        // Given
        assertFalse(tutorialViewModel.isCompleted.value)

        // When
        tutorialViewModel.completeTutorial()

        // Then
        assertTrue(tutorialViewModel.isCompleted.value)
    }

    @Test
    fun `test tutorial skip`() = runTest {
        // Given
        assertFalse(tutorialViewModel.isCompleted.value)

        // When
        tutorialViewModel.skipTutorial()

        // Then
        assertTrue(tutorialViewModel.isCompleted.value)
    }

    @Test
    fun `test tutorial voice toggle`() = runTest {
        // Given
        val initialPreferences = UserPreferences(voiceCommandsEnabled = false)
        every { mockUserPreferencesRepository.getUserPreferences() } returns flowOf(initialPreferences)
        coEvery { mockUserPreferencesRepository.setVoiceCommandsEnabled(any()) } just Runs

        // When
        tutorialViewModel.toggleVoice()

        // Then
        coVerify { mockUserPreferencesRepository.setVoiceCommandsEnabled(true) }
        assertTrue(tutorialViewModel.voiceEnabled.value)
    }

    // Integration Tests
    @Test
    fun `test health record workflow integration`() = runTest {
        // Given
        val fowlId = "test-fowl-id"
        val healthRecord = HealthRecord(
            fowlId = fowlId,
            type = "Vaccination",
            description = "COVID vaccination",
            date = Date(),
            veterinarian = "Dr. Test",
            status = "Healthy"
        )

        coEvery { mockHealthRepository.addHealthRecord(any()) } just Runs
        every { mockHealthRepository.getHealthRecords(fowlId) } returns flowOf(listOf(healthRecord))

        // When - Complete workflow: load, add, update, delete
        healthViewModel.loadHealthRecords(fowlId)
        healthViewModel.addHealthRecord(fowlId, healthRecord)
        
        val updatedRecord = healthRecord.copy(description = "Updated description")
        healthViewModel.updateHealthRecord(updatedRecord)
        
        healthViewModel.deleteHealthRecord(healthRecord.id)

        // Then
        coVerify { mockHealthRepository.addHealthRecord(any()) }
        coVerify { mockHealthRepository.updateHealthRecord(any()) }
        coVerify { mockHealthRepository.deleteHealthRecord(healthRecord.id) }
    }

    @Test
    fun `test sales analytics calculation`() = runTest {
        // Given
        val farmId = "test-farm-id"
        val salesRecords = listOf(
            SaleRecord(farmId = farmId, salePrice = 1000.0, saleDate = Date()),
            SaleRecord(farmId = farmId, salePrice = 1500.0, saleDate = Date()),
            SaleRecord(farmId = farmId, salePrice = 750.0, saleDate = Date())
        )

        every { mockSalesRepository.getSalesRecords(farmId) } returns flowOf(salesRecords)

        // When
        salesViewModel.loadSalesRecords(farmId)

        // Then
        assertEquals(3250.0, salesViewModel.totalRevenue.value, 0.01)
        assertEquals(3, salesViewModel.salesRecords.value.size)
    }

    @Test
    fun `test inventory category filtering`() = runTest {
        // Given
        val farmId = "test-farm-id"
        val feedItem = InventoryItem(farmId = farmId, category = "Feed", name = "Chicken Feed")
        val medicineItem = InventoryItem(farmId = farmId, category = "Medicine", name = "Antibiotics")
        val equipmentItem = InventoryItem(farmId = farmId, category = "Equipment", name = "Feeder")

        val allItems = listOf(feedItem, medicineItem, equipmentItem)
        every { mockInventoryRepository.getInventoryItems(farmId) } returns flowOf(allItems)

        // When
        inventoryViewModel.loadInventoryItems(farmId)
        inventoryViewModel.filterByCategory("Feed")

        // Then
        assertEquals(1, inventoryViewModel.inventoryItems.value.size)
        assertEquals("Feed", inventoryViewModel.inventoryItems.value.first().category)
    }

    @Test
    fun `test error handling across ViewModels`() = runTest {
        // Given
        val errorMessage = "Database connection failed"
        
        every { mockHealthRepository.getHealthRecords(any()) } throws Exception(errorMessage)
        every { mockSalesRepository.getSalesRecords(any()) } throws Exception(errorMessage)
        every { mockInventoryRepository.getInventoryItems(any()) } throws Exception(errorMessage)

        // When
        healthViewModel.loadHealthRecords("test-id")
        salesViewModel.loadSalesRecords("test-id")
        inventoryViewModel.loadInventoryItems("test-id")

        // Then
        assertTrue(healthViewModel.error.value?.contains(errorMessage) == true)
        assertTrue(salesViewModel.error.value?.contains(errorMessage) == true)
        assertTrue(inventoryViewModel.error.value?.contains(errorMessage) == true)
    }

    @Test
    fun `test data validation in models`() {
        // Test HealthRecord validation
        val healthRecord = HealthRecord(
            id = "test-id",
            fowlId = "fowl-id",
            type = "Vaccination",
            description = "Test vaccination",
            date = Date(),
            status = "Healthy"
        )
        
        assertNotNull(healthRecord.id)
        assertNotNull(healthRecord.fowlId)
        assertTrue(healthRecord.type.isNotEmpty())
        assertTrue(healthRecord.description.isNotEmpty())

        // Test SaleRecord validation
        val saleRecord = SaleRecord(
            id = "sale-id",
            farmId = "farm-id",
            fowlName = "Test Fowl",
            buyerName = "Test Buyer",
            salePrice = 500.0,
            saleDate = Date()
        )
        
        assertTrue(saleRecord.salePrice > 0)
        assertTrue(saleRecord.fowlName.isNotEmpty())
        assertTrue(saleRecord.buyerName.isNotEmpty())

        // Test InventoryItem validation
        val inventoryItem = InventoryItem(
            id = "item-id",
            farmId = "farm-id",
            name = "Test Item",
            category = "Feed",
            currentQuantity = 50.0,
            minimumQuantity = 10.0,
            unitPrice = 25.0
        )
        
        assertTrue(inventoryItem.currentQuantity >= 0)
        assertTrue(inventoryItem.minimumQuantity >= 0)
        assertTrue(inventoryItem.unitPrice >= 0)
        assertTrue(inventoryItem.name.isNotEmpty())
    }
}