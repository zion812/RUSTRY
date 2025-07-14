package com.rio.rustry.data.local.dao

import androidx.room.*
import com.rio.rustry.data.local.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * Enhanced Farm DAO with comprehensive offline support
 */
@Dao
interface FarmDao {
    
    // Basic CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarm(farm: FarmEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarms(farms: List<FarmEntity>)
    
    @Update
    suspend fun updateFarm(farm: FarmEntity)
    
    @Query("UPDATE farms SET is_deleted = 1, updated_at = :timestamp, needs_sync = 1 WHERE id = :farmId")
    suspend fun softDeleteFarm(farmId: String, timestamp: Long)
    
    @Query("DELETE FROM farms WHERE id = :farmId")
    suspend fun hardDeleteFarm(farmId: String)
    
    // Query operations
    @Query("SELECT * FROM farms WHERE id = :farmId AND is_deleted = 0")
    suspend fun getFarmById(farmId: String): FarmEntity?
    
    @Query("SELECT * FROM farms WHERE id = :farmId AND is_deleted = 0")
    fun getFarmByIdFlow(farmId: String): Flow<FarmEntity?>
    
    @Query("SELECT * FROM farms WHERE owner_id = :ownerId AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getFarmsByOwner(ownerId: String): Flow<List<FarmEntity>>
    
    @Query("SELECT * FROM farms WHERE is_deleted = 0 ORDER BY updated_at DESC")
    fun getAllFarms(): Flow<List<FarmEntity>>
    
    @Query("SELECT * FROM farms WHERE (name LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%') AND is_deleted = 0 ORDER BY updated_at DESC")
    fun searchFarms(query: String): Flow<List<FarmEntity>>
    
    @Query("SELECT * FROM farms WHERE location LIKE '%' || :location || '%' AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getFarmsByLocation(location: String): Flow<List<FarmEntity>>
    
    @Query("SELECT * FROM farms WHERE size BETWEEN :minSize AND :maxSize AND is_deleted = 0 ORDER BY size ASC")
    fun getFarmsBySizeRange(minSize: Double, maxSize: Double): Flow<List<FarmEntity>>
    
    @Query("SELECT * FROM farms WHERE farm_type = :type AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getFarmsByType(type: String): Flow<List<FarmEntity>>
    
    @Query("SELECT * FROM farms WHERE is_verified = :verified AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getVerifiedFarms(verified: Boolean): Flow<List<FarmEntity>>
    
    // Sync operations
    @Query("SELECT * FROM farms WHERE needs_sync = 1")
    suspend fun getFarmsNeedingSync(): List<FarmEntity>
    
    @Query("UPDATE farms SET needs_sync = 0, last_synced_at = :timestamp WHERE id = :farmId")
    suspend fun markFarmAsSynced(farmId: String, timestamp: Long)
    
    @Query("UPDATE farms SET needs_sync = 1 WHERE id = :farmId")
    suspend fun markFarmForSync(farmId: String)
    
    // Statistics
    @Query("SELECT COUNT(*) FROM farms WHERE owner_id = :ownerId AND is_deleted = 0")
    suspend fun getFarmCountByOwner(ownerId: String): Int
    
    @Query("SELECT COUNT(*) FROM farms WHERE is_deleted = 0")
    suspend fun getTotalFarmCount(): Int
    
    @Query("SELECT AVG(size) FROM farms WHERE owner_id = :ownerId AND is_deleted = 0")
    suspend fun getAverageFarmSize(ownerId: String): Double
    
    @Query("SELECT SUM(size) FROM farms WHERE owner_id = :ownerId AND is_deleted = 0")
    suspend fun getTotalFarmSize(ownerId: String): Double
    
    // Cleanup operations
    @Query("DELETE FROM farms WHERE is_deleted = 1 AND updated_at < :cutoffTime")
    suspend fun cleanupOldDeletedFarms(cutoffTime: Long)
    
    @Query("SELECT * FROM farms WHERE created_at BETWEEN :startDate AND :endDate AND is_deleted = 0")
    fun getFarmsByDateRange(startDate: Long, endDate: Long): Flow<List<FarmEntity>>
}

/**
 * Enhanced Flock DAO with comprehensive tracking
 */
@Dao
interface FlockDao {
    
    // Basic CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlock(flock: FlockEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlocks(flocks: List<FlockEntity>)
    
    @Update
    suspend fun updateFlock(flock: FlockEntity)
    
    @Query("UPDATE flocks SET is_deleted = 1, updated_at = :timestamp, needs_sync = 1 WHERE id = :flockId")
    suspend fun softDeleteFlock(flockId: String, timestamp: Long)
    
    @Query("DELETE FROM flocks WHERE id = :flockId")
    suspend fun hardDeleteFlock(flockId: String)
    
    // Query operations
    @Query("SELECT * FROM flocks WHERE id = :flockId AND is_deleted = 0")
    suspend fun getFlockById(flockId: String): FlockEntity?
    
    @Query("SELECT * FROM flocks WHERE id = :flockId AND is_deleted = 0")
    fun getFlockByIdFlow(flockId: String): Flow<FlockEntity?>
    
    @Query("SELECT * FROM flocks WHERE farm_id = :farmId AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getFlocksByFarm(farmId: String): Flow<List<FlockEntity>>
    
    @Query("SELECT * FROM flocks WHERE breed LIKE '%' || :breed || '%' AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getFlocksByBreed(breed: String): Flow<List<FlockEntity>>
    
    @Query("SELECT * FROM flocks WHERE health_status = :status AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getFlocksByHealthStatus(status: String): Flow<List<FlockEntity>>
    
    @Query("SELECT * FROM flocks WHERE vaccination_status = :status AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getFlocksByVaccinationStatus(status: String): Flow<List<FlockEntity>>
    
    @Query("SELECT * FROM flocks WHERE is_for_sale = 1 AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getFlocksForSale(): Flow<List<FlockEntity>>
    
    @Query("SELECT * FROM flocks WHERE production_type = :type AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getFlocksByProductionType(type: String): Flow<List<FlockEntity>>
    
    @Query("SELECT * FROM flocks WHERE age_months BETWEEN :minAge AND :maxAge AND is_deleted = 0 ORDER BY age_months ASC")
    fun getFlocksByAgeRange(minAge: Int, maxAge: Int): Flow<List<FlockEntity>>
    
    // Sync operations
    @Query("SELECT * FROM flocks WHERE needs_sync = 1")
    suspend fun getFlocksNeedingSync(): List<FlockEntity>
    
    @Query("UPDATE flocks SET needs_sync = 0, last_synced_at = :timestamp WHERE id = :flockId")
    suspend fun markFlockAsSynced(flockId: String, timestamp: Long)
    
    // Statistics
    @Query("SELECT COUNT(*) FROM flocks WHERE farm_id = :farmId AND is_deleted = 0")
    suspend fun getFlockCountByFarm(farmId: String): Int
    
    @Query("SELECT SUM(quantity) FROM flocks WHERE farm_id = :farmId AND is_deleted = 0")
    suspend fun getTotalBirdsByFarm(farmId: String): Int
    
    @Query("SELECT SUM(quantity) FROM flocks WHERE farm_id = :farmId AND health_status = 'HEALTHY' AND is_deleted = 0")
    suspend fun getHealthyBirdsByFarm(farmId: String): Int
    
    @Query("SELECT SUM(quantity) FROM flocks WHERE farm_id = :farmId AND health_status = 'SICK' AND is_deleted = 0")
    suspend fun getSickBirdsByFarm(farmId: String): Int
    
    @Query("SELECT AVG(age_months) FROM flocks WHERE farm_id = :farmId AND is_deleted = 0")
    suspend fun getAverageFlockAge(farmId: String): Double
    
    // Cleanup operations
    @Query("DELETE FROM flocks WHERE is_deleted = 1 AND updated_at < :cutoffTime")
    suspend fun cleanupOldDeletedFlocks(cutoffTime: Long)
}

/**
 * Enhanced Health Record DAO with comprehensive tracking
 */
@Dao
interface HealthRecordDao {
    
    // Basic CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthRecord(record: HealthRecordEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthRecords(records: List<HealthRecordEntity>)
    
    @Update
    suspend fun updateHealthRecord(record: HealthRecordEntity)
    
    @Query("UPDATE health_records SET is_deleted = 1, updated_at = :timestamp, needs_sync = 1 WHERE id = :recordId")
    suspend fun softDeleteHealthRecord(recordId: String, timestamp: Long)
    
    @Query("DELETE FROM health_records WHERE id = :recordId")
    suspend fun hardDeleteHealthRecord(recordId: String)
    
    // Query operations
    @Query("SELECT * FROM health_records WHERE id = :recordId AND is_deleted = 0")
    suspend fun getHealthRecordById(recordId: String): HealthRecordEntity?
    
    @Query("SELECT * FROM health_records WHERE id = :recordId AND is_deleted = 0")
    fun getHealthRecordByIdFlow(recordId: String): Flow<HealthRecordEntity?>
    
    @Query("SELECT * FROM health_records WHERE flock_id = :flockId AND is_deleted = 0 ORDER BY date DESC")
    fun getHealthRecordsByFlock(flockId: String): Flow<List<HealthRecordEntity>>
    
    @Query("SELECT * FROM health_records WHERE farm_id = :farmId AND is_deleted = 0 ORDER BY date DESC")
    fun getHealthRecordsByFarm(farmId: String): Flow<List<HealthRecordEntity>>
    
    @Query("SELECT * FROM health_records WHERE type = :type AND is_deleted = 0 ORDER BY date DESC")
    fun getHealthRecordsByType(type: String): Flow<List<HealthRecordEntity>>
    
    @Query("SELECT * FROM health_records WHERE date BETWEEN :startDate AND :endDate AND is_deleted = 0 ORDER BY date DESC")
    fun getHealthRecordsByDateRange(startDate: Long, endDate: Long): Flow<List<HealthRecordEntity>>
    
    @Query("SELECT * FROM health_records WHERE next_due_date <= :currentTime AND is_deleted = 0 ORDER BY next_due_date ASC")
    fun getOverdueHealthRecords(currentTime: Long): Flow<List<HealthRecordEntity>>
    
    @Query("SELECT * FROM health_records WHERE next_due_date BETWEEN :startTime AND :endTime AND is_deleted = 0 ORDER BY next_due_date ASC")
    fun getUpcomingHealthRecords(startTime: Long, endTime: Long): Flow<List<HealthRecordEntity>>
    
    @Query("SELECT * FROM health_records WHERE is_emergency = 1 AND is_deleted = 0 ORDER BY date DESC")
    fun getEmergencyHealthRecords(): Flow<List<HealthRecordEntity>>
    
    @Query("SELECT * FROM health_records WHERE follow_up_required = 1 AND is_deleted = 0 ORDER BY follow_up_date ASC")
    fun getRecordsRequiringFollowUp(): Flow<List<HealthRecordEntity>>
    
    // Sync operations
    @Query("SELECT * FROM health_records WHERE needs_sync = 1")
    suspend fun getHealthRecordsNeedingSync(): List<HealthRecordEntity>
    
    @Query("UPDATE health_records SET needs_sync = 0, last_synced_at = :timestamp WHERE id = :recordId")
    suspend fun markHealthRecordAsSynced(recordId: String, timestamp: Long)
    
    // Statistics
    @Query("SELECT COUNT(*) FROM health_records WHERE farm_id = :farmId AND is_deleted = 0")
    suspend fun getHealthRecordCountByFarm(farmId: String): Int
    
    @Query("SELECT COUNT(*) FROM health_records WHERE flock_id = :flockId AND type = :type AND is_deleted = 0")
    suspend fun getHealthRecordCountByFlockAndType(flockId: String, type: String): Int
    
    @Query("SELECT SUM(cost) FROM health_records WHERE farm_id = :farmId AND date BETWEEN :startDate AND :endDate AND is_deleted = 0")
    suspend fun getTotalHealthCostByFarmAndPeriod(farmId: String, startDate: Long, endDate: Long): Double
    
    // Cleanup operations
    @Query("DELETE FROM health_records WHERE is_deleted = 1 AND updated_at < :cutoffTime")
    suspend fun cleanupOldDeletedHealthRecords(cutoffTime: Long)
}

/**
 * Enhanced Sale DAO with comprehensive transaction tracking
 */
@Dao
interface SaleDao {
    
    // Basic CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSales(sales: List<SaleEntity>)
    
    @Update
    suspend fun updateSale(sale: SaleEntity)
    
    @Query("UPDATE sales SET is_deleted = 1, updated_at = :timestamp, needs_sync = 1 WHERE id = :saleId")
    suspend fun softDeleteSale(saleId: String, timestamp: Long)
    
    @Query("DELETE FROM sales WHERE id = :saleId")
    suspend fun hardDeleteSale(saleId: String)
    
    // Query operations
    @Query("SELECT * FROM sales WHERE id = :saleId AND is_deleted = 0")
    suspend fun getSaleById(saleId: String): SaleEntity?
    
    @Query("SELECT * FROM sales WHERE id = :saleId AND is_deleted = 0")
    fun getSaleByIdFlow(saleId: String): Flow<SaleEntity?>
    
    @Query("SELECT * FROM sales WHERE farm_id = :farmId AND is_deleted = 0 ORDER BY sale_date DESC")
    fun getSalesByFarm(farmId: String): Flow<List<SaleEntity>>
    
    @Query("SELECT * FROM sales WHERE buyer_name LIKE '%' || :buyerName || '%' AND is_deleted = 0 ORDER BY sale_date DESC")
    fun getSalesByBuyer(buyerName: String): Flow<List<SaleEntity>>
    
    @Query("SELECT * FROM sales WHERE sale_date BETWEEN :startDate AND :endDate AND is_deleted = 0 ORDER BY sale_date DESC")
    fun getSalesByDateRange(startDate: Long, endDate: Long): Flow<List<SaleEntity>>
    
    @Query("SELECT * FROM sales WHERE payment_status = :status AND is_deleted = 0 ORDER BY sale_date DESC")
    fun getSalesByPaymentStatus(status: String): Flow<List<SaleEntity>>
    
    @Query("SELECT * FROM sales WHERE delivery_status = :status AND is_deleted = 0 ORDER BY sale_date DESC")
    fun getSalesByDeliveryStatus(status: String): Flow<List<SaleEntity>>
    
    @Query("SELECT * FROM sales WHERE payment_method = :method AND is_deleted = 0 ORDER BY sale_date DESC")
    fun getSalesByPaymentMethod(method: String): Flow<List<SaleEntity>>
    
    @Query("SELECT * FROM sales WHERE amount BETWEEN :minAmount AND :maxAmount AND is_deleted = 0 ORDER BY amount DESC")
    fun getSalesByAmountRange(minAmount: Double, maxAmount: Double): Flow<List<SaleEntity>>
    
    // Sync operations
    @Query("SELECT * FROM sales WHERE needs_sync = 1")
    suspend fun getSalesNeedingSync(): List<SaleEntity>
    
    @Query("UPDATE sales SET needs_sync = 0, last_synced_at = :timestamp WHERE id = :saleId")
    suspend fun markSaleAsSynced(saleId: String, timestamp: Long)
    
    // Statistics
    @Query("SELECT COUNT(*) FROM sales WHERE farm_id = :farmId AND is_deleted = 0")
    suspend fun getSaleCountByFarm(farmId: String): Int
    
    @Query("SELECT SUM(total_amount) FROM sales WHERE farm_id = :farmId AND is_deleted = 0")
    suspend fun getTotalRevenueByFarm(farmId: String): Double
    
    @Query("SELECT SUM(total_amount) FROM sales WHERE farm_id = :farmId AND sale_date BETWEEN :startDate AND :endDate AND is_deleted = 0")
    suspend fun getRevenueByFarmAndPeriod(farmId: String, startDate: Long, endDate: Long): Double
    
    @Query("SELECT AVG(total_amount) FROM sales WHERE farm_id = :farmId AND is_deleted = 0")
    suspend fun getAverageSaleAmount(farmId: String): Double
    
    @Query("SELECT COUNT(DISTINCT buyer_name) FROM sales WHERE farm_id = :farmId AND is_deleted = 0")
    suspend fun getUniqueCustomerCount(farmId: String): Int
    
    // Cleanup operations
    @Query("DELETE FROM sales WHERE is_deleted = 1 AND updated_at < :cutoffTime")
    suspend fun cleanupOldDeletedSales(cutoffTime: Long)
}

/**
 * Enhanced Inventory DAO with comprehensive stock management
 */
@Dao
interface InventoryDao {
    
    // Basic CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItems(items: List<InventoryEntity>)
    
    @Update
    suspend fun updateInventoryItem(item: InventoryEntity)
    
    @Query("UPDATE inventory SET is_deleted = 1, updated_at = :timestamp, needs_sync = 1 WHERE id = :itemId")
    suspend fun softDeleteInventoryItem(itemId: String, timestamp: Long)
    
    @Query("DELETE FROM inventory WHERE id = :itemId")
    suspend fun hardDeleteInventoryItem(itemId: String)
    
    // Query operations
    @Query("SELECT * FROM inventory WHERE id = :itemId AND is_deleted = 0")
    suspend fun getInventoryItemById(itemId: String): InventoryEntity?
    
    @Query("SELECT * FROM inventory WHERE id = :itemId AND is_deleted = 0")
    fun getInventoryItemByIdFlow(itemId: String): Flow<InventoryEntity?>
    
    @Query("SELECT * FROM inventory WHERE farm_id = :farmId AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getInventoryByFarm(farmId: String): Flow<List<InventoryEntity>>
    
    @Query("SELECT * FROM inventory WHERE farm_id = :farmId AND type = :type AND is_deleted = 0 ORDER BY name ASC")
    fun getInventoryByFarmAndType(farmId: String, type: String): Flow<List<InventoryEntity>>
    
    @Query("SELECT * FROM inventory WHERE farm_id = :farmId AND quantity <= restock_threshold AND is_deleted = 0 ORDER BY quantity ASC")
    fun getLowStockItems(farmId: String): Flow<List<InventoryEntity>>
    
    @Query("SELECT * FROM inventory WHERE farm_id = :farmId AND expiry_date <= :currentTime AND is_deleted = 0 ORDER BY expiry_date ASC")
    fun getExpiredItems(farmId: String, currentTime: Long): Flow<List<InventoryEntity>>
    
    @Query("SELECT * FROM inventory WHERE farm_id = :farmId AND expiry_date BETWEEN :currentTime AND :warningTime AND is_deleted = 0 ORDER BY expiry_date ASC")
    fun getExpiringItems(farmId: String, currentTime: Long, warningTime: Long): Flow<List<InventoryEntity>>
    
    @Query("SELECT * FROM inventory WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' AND is_deleted = 0 ORDER BY name ASC")
    fun searchInventoryItems(query: String): Flow<List<InventoryEntity>>
    
    @Query("SELECT * FROM inventory WHERE supplier_name LIKE '%' || :supplier || '%' AND is_deleted = 0 ORDER BY name ASC")
    fun getInventoryBySupplier(supplier: String): Flow<List<InventoryEntity>>
    
    @Query("SELECT * FROM inventory WHERE is_active = :active AND is_deleted = 0 ORDER BY name ASC")
    fun getInventoryByActiveStatus(active: Boolean): Flow<List<InventoryEntity>>
    
    // Sync operations
    @Query("SELECT * FROM inventory WHERE needs_sync = 1")
    suspend fun getInventoryItemsNeedingSync(): List<InventoryEntity>
    
    @Query("UPDATE inventory SET needs_sync = 0, last_synced_at = :timestamp WHERE id = :itemId")
    suspend fun markInventoryItemAsSynced(itemId: String, timestamp: Long)
    
    // Statistics
    @Query("SELECT COUNT(*) FROM inventory WHERE farm_id = :farmId AND is_deleted = 0")
    suspend fun getInventoryItemCountByFarm(farmId: String): Int
    
    @Query("SELECT COUNT(*) FROM inventory WHERE farm_id = :farmId AND quantity <= restock_threshold AND is_deleted = 0")
    suspend fun getLowStockItemCount(farmId: String): Int
    
    @Query("SELECT SUM(total_cost) FROM inventory WHERE farm_id = :farmId AND is_deleted = 0")
    suspend fun getTotalInventoryValue(farmId: String): Double
    
    @Query("SELECT SUM(total_cost) FROM inventory WHERE farm_id = :farmId AND type = :type AND is_deleted = 0")
    suspend fun getInventoryValueByType(farmId: String, type: String): Double
    
    // Cleanup operations
    @Query("DELETE FROM inventory WHERE is_deleted = 1 AND updated_at < :cutoffTime")
    suspend fun cleanupOldDeletedInventoryItems(cutoffTime: Long)
    
    @Query("UPDATE inventory SET alert_sent = 0")
    suspend fun resetAllAlerts()
}

/**
 * Change Log DAO for audit trail
 */
@Dao
interface ChangeLogDao {
    
    // Basic operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChangeLog(log: ChangeLogEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChangeLogs(logs: List<ChangeLogEntity>)
    
    @Update
    suspend fun updateChangeLog(log: ChangeLogEntity)
    
    @Query("DELETE FROM change_logs WHERE id = :logId")
    suspend fun deleteChangeLog(logId: String)
    
    // Query operations
    @Query("SELECT * FROM change_logs WHERE id = :logId")
    suspend fun getChangeLogById(logId: String): ChangeLogEntity?
    
    @Query("SELECT * FROM change_logs WHERE farm_id = :farmId ORDER BY timestamp DESC")
    fun getChangeLogsByFarm(farmId: String): Flow<List<ChangeLogEntity>>
    
    @Query("SELECT * FROM change_logs WHERE user_id = :userId ORDER BY timestamp DESC")
    fun getChangeLogsByUser(userId: String): Flow<List<ChangeLogEntity>>
    
    @Query("SELECT * FROM change_logs WHERE entity_type = :entityType AND entity_id = :entityId ORDER BY timestamp DESC")
    fun getChangeLogsByEntity(entityType: String, entityId: String): Flow<List<ChangeLogEntity>>
    
    @Query("SELECT * FROM change_logs WHERE action = :action ORDER BY timestamp DESC")
    fun getChangeLogsByAction(action: String): Flow<List<ChangeLogEntity>>
    
    @Query("SELECT * FROM change_logs WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getChangeLogsByTimeRange(startTime: Long, endTime: Long): Flow<List<ChangeLogEntity>>
    
    @Query("SELECT * FROM change_logs WHERE is_verified = :verified ORDER BY timestamp DESC")
    fun getChangeLogsByVerificationStatus(verified: Boolean): Flow<List<ChangeLogEntity>>
    
    // Sync operations
    @Query("SELECT * FROM change_logs WHERE needs_sync = 1")
    suspend fun getChangeLogsNeedingSync(): List<ChangeLogEntity>
    
    @Query("UPDATE change_logs SET needs_sync = 0, last_synced_at = :timestamp WHERE id = :logId")
    suspend fun markChangeLogAsSynced(logId: String, timestamp: Long)
    
    // Statistics
    @Query("SELECT COUNT(*) FROM change_logs WHERE farm_id = :farmId")
    suspend fun getChangeLogCountByFarm(farmId: String): Int
    
    @Query("SELECT COUNT(*) FROM change_logs WHERE user_id = :userId")
    suspend fun getChangeLogCountByUser(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM change_logs WHERE action = :action")
    suspend fun getChangeLogCountByAction(action: String): Int
    
    // Cleanup operations
    @Query("DELETE FROM change_logs WHERE timestamp < :cutoffTime")
    suspend fun cleanupOldChangeLogs(cutoffTime: Long)
}