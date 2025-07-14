package com.rio.rustry.domain.repository

import com.rio.rustry.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Farm operations
 * Defines all farm-related data operations
 */
interface FarmRepository {
    
    // Farm operations
    suspend fun addFarm(farm: Farm): String
    suspend fun updateFarm(farm: Farm)
    suspend fun deleteFarm(farmId: String)
    fun getFarmById(farmId: String): Flow<Farm?>
    fun getFarmsByOwner(ownerId: String): Flow<List<Farm>>
    fun getAllFarms(): Flow<List<Farm>>
    fun searchFarms(query: String): Flow<List<Farm>>
    
    // Flock operations
    suspend fun addFlock(flock: Flock): String
    suspend fun updateFlock(flock: Flock)
    suspend fun deleteFlock(flockId: String)
    fun getFlockById(flockId: String): Flow<Flock?>
    fun getFlocksByFarm(farmId: String): Flow<List<Flock>>
    
    // Health record operations
    suspend fun addHealthRecord(record: HealthRecord): String
    suspend fun updateHealthRecord(record: HealthRecord)
    suspend fun deleteHealthRecord(recordId: String)
    fun getHealthRecordById(recordId: String): Flow<HealthRecord?>
    fun getHealthRecordsByFlock(flockId: String): Flow<List<HealthRecord>>
    fun getHealthRecordsByFarm(farmId: String): Flow<List<HealthRecord>>
    
    // Sales operations
    suspend fun addSale(sale: Sale): String
    suspend fun updateSale(sale: Sale)
    suspend fun deleteSale(saleId: String)
    fun getSaleById(saleId: String): Flow<Sale?>
    fun getSalesByFarm(farmId: String): Flow<List<Sale>>
    
    // Inventory operations
    suspend fun addInventoryItem(item: InventoryItem): String
    suspend fun updateInventoryItem(item: InventoryItem)
    suspend fun deleteInventoryItem(itemId: String)
    fun getInventoryItemById(itemId: String): Flow<InventoryItem?>
    fun getInventoryByFarm(farmId: String): Flow<List<InventoryItem>>
    fun getLowStockItems(farmId: String): Flow<List<InventoryItem>>
    
    // Change log operations
    suspend fun addChangeLog(log: ChangeLog): String
    fun getChangeLogsByFarm(farmId: String): Flow<List<ChangeLog>>
    fun getChangeLogsByUser(userId: String): Flow<List<ChangeLog>>
    
    // Notification operations
    suspend fun addNotification(notification: FarmNotification): String
    suspend fun markNotificationAsRead(notificationId: String)
    fun getNotificationsByUser(userId: String): Flow<List<FarmNotification>>
    fun getUnreadNotifications(userId: String): Flow<List<FarmNotification>>
    
    // Statistics and analytics
    fun getFarmStatistics(farmId: String): Flow<FarmStatistics>
    fun getOverallStatistics(userId: String): Flow<Map<String, Any>>
    
    // Photo and media operations
    suspend fun uploadFarmPhoto(farmId: String, photoUri: String): String
    suspend fun uploadFlockPhoto(flockId: String, photoUri: String): String
    suspend fun uploadHealthRecordPhoto(recordId: String, photoUri: String): String
    suspend fun validatePhoto(photoUri: String): PhotoValidationResult
    
    // Offline and sync operations
    suspend fun syncOfflineChanges()
    suspend fun markAsSynced(entityType: String, entityId: String)
    fun getUnsyncedEntities(): Flow<List<Any>>
    
    // Backup and restore
    suspend fun backupFarmData(farmId: String): String
    suspend fun restoreFarmData(farmId: String, backupData: String)
    
    // Search and filter operations
    fun searchFarmsByLocation(location: String): Flow<List<Farm>>
    fun searchFlocksByBreed(breed: String): Flow<List<Flock>>
    fun getHealthRecordsByType(type: String): Flow<List<HealthRecord>>
    fun getSalesByDateRange(startDate: Long, endDate: Long): Flow<List<Sale>>
    
    // Reminder and notification scheduling
    suspend fun scheduleVaccinationReminder(flockId: String, dueDate: Long)
    suspend fun scheduleRestockAlert(itemId: String, threshold: Int)
    suspend fun cancelScheduledNotification(notificationId: String)
    
    // Data validation and integrity
    suspend fun validateFarmData(farm: Farm): List<String>
    suspend fun validateFlockData(flock: Flock): List<String>
    suspend fun validateHealthRecordData(record: HealthRecord): List<String>
    suspend fun validateSaleData(sale: Sale): List<String>
    suspend fun validateInventoryData(item: InventoryItem): List<String>
    
    // Export and reporting
    suspend fun exportFarmData(farmId: String, format: String): String
    suspend fun generateFarmReport(farmId: String, reportType: String): String
    suspend fun generateHealthReport(flockId: String): String
    suspend fun generateSalesReport(farmId: String, period: String): String
    
    // User preferences and settings
    suspend fun updateFarmSettings(farmId: String, settings: Map<String, Any>)
    fun getFarmSettings(farmId: String): Flow<Map<String, Any>>
    
    // Geolocation and mapping
    suspend fun updateFarmLocation(farmId: String, latitude: Double, longitude: Double)
    fun getNearbyFarms(latitude: Double, longitude: Double, radiusKm: Double): Flow<List<Farm>>
    
    // Integration with external services
    suspend fun syncWithWeatherService(farmId: String): Map<String, Any>
    suspend fun syncWithMarketPrices(location: String): Map<String, Double>
    suspend fun syncWithVeterinaryServices(location: String): List<Map<String, Any>>
}