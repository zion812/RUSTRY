package com.rio.rustry.data.local.enhanced

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rio.rustry.data.local.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * Enhanced Room Database for RUSTRY with comprehensive offline support
 */
@Database(
    entities = [
        EnhancedFowlEntity::class,
        EnhancedUserEntity::class,
        SyncQueueEntity::class,
        OfflineActionEntity::class,
        CachedImageEntity::class,
        NotificationEntity::class,
        TransactionEntity::class,
        TraceabilityEntity::class,
        FarmEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class EnhancedRustryDatabase : RoomDatabase() {
    
    abstract fun fowlDao(): EnhancedFowlDao
    abstract fun userDao(): EnhancedUserDao
    abstract fun syncDao(): SyncDao
    abstract fun offlineActionDao(): OfflineActionDao
    abstract fun cachedImageDao(): CachedImageDao
    abstract fun notificationDao(): NotificationDao
    abstract fun transactionDao(): TransactionDao
    abstract fun traceabilityDao(): TraceabilityDao
    abstract fun farmDao(): FarmDao
    
    companion object {
        const val DATABASE_NAME = "rustry_enhanced_database"
        
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new tables for enhanced functionality
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS sync_queue (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        entity_type TEXT NOT NULL,
                        entity_id TEXT NOT NULL,
                        action TEXT NOT NULL,
                        data TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        retry_count INTEGER NOT NULL DEFAULT 0,
                        status TEXT NOT NULL DEFAULT 'PENDING'
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS offline_actions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        action_type TEXT NOT NULL,
                        target_id TEXT NOT NULL,
                        action_data TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        is_synced INTEGER NOT NULL DEFAULT 0
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS cached_images (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        image_url TEXT NOT NULL,
                        local_path TEXT NOT NULL,
                        entity_id TEXT NOT NULL,
                        entity_type TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        file_size INTEGER NOT NULL DEFAULT 0
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS notifications (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        notification_id TEXT NOT NULL,
                        title TEXT NOT NULL,
                        body TEXT NOT NULL,
                        type TEXT NOT NULL,
                        data TEXT,
                        timestamp INTEGER NOT NULL,
                        is_read INTEGER NOT NULL DEFAULT 0,
                        is_synced INTEGER NOT NULL DEFAULT 0
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        transaction_id TEXT NOT NULL,
                        fowl_id TEXT NOT NULL,
                        buyer_id TEXT NOT NULL,
                        seller_id TEXT NOT NULL,
                        amount REAL NOT NULL,
                        status TEXT NOT NULL,
                        payment_method TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        is_synced INTEGER NOT NULL DEFAULT 0
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS traceability (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        fowl_id TEXT NOT NULL,
                        event_type TEXT NOT NULL,
                        event_data TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        verified_by TEXT,
                        is_synced INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS farms (
                        id TEXT PRIMARY KEY NOT NULL,
                        owner_id TEXT NOT NULL,
                        name TEXT NOT NULL,
                        location TEXT NOT NULL,
                        size REAL NOT NULL,
                        photo_url TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL,
                        is_deleted INTEGER NOT NULL DEFAULT 0,
                        needs_sync INTEGER NOT NULL DEFAULT 1,
                        last_synced_at INTEGER,
                        offline_changes TEXT NOT NULL DEFAULT '{}'
                    )
                """)
            }
        }
    }
}

/**
 * Enhanced Fowl DAO with offline support
 */
@Dao
interface EnhancedFowlDao {
    
    @Query("SELECT * FROM fowls WHERE is_deleted = 0 ORDER BY updated_at DESC")
    fun getAllFowls(): Flow<List<EnhancedFowlEntity>>
    
    @Query("SELECT * FROM fowls WHERE is_for_sale = 1 AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getMarketplaceFowls(): Flow<List<EnhancedFowlEntity>>
    
    @Query("SELECT * FROM fowls WHERE owner_id = :ownerId AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getFowlsByOwner(ownerId: String): Flow<List<EnhancedFowlEntity>>
    
    @Query("SELECT * FROM fowls WHERE id = :fowlId AND is_deleted = 0")
    suspend fun getFowlById(fowlId: String): EnhancedFowlEntity?
    
    @Query("SELECT * FROM fowls WHERE breed LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' AND is_deleted = 0")
    fun searchFowls(query: String): Flow<List<EnhancedFowlEntity>>
    
    @Query("SELECT * FROM fowls WHERE location LIKE '%' || :location || '%' AND is_deleted = 0")
    fun getFowlsByLocation(location: String): Flow<List<EnhancedFowlEntity>>
    
    @Query("SELECT * FROM fowls WHERE price BETWEEN :minPrice AND :maxPrice AND is_deleted = 0")
    fun getFowlsByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<EnhancedFowlEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFowl(fowl: EnhancedFowlEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFowls(fowls: List<EnhancedFowlEntity>)
    
    @Update
    suspend fun updateFowl(fowl: EnhancedFowlEntity)
    
    @Query("UPDATE fowls SET is_deleted = 1, updated_at = :timestamp WHERE id = :fowlId")
    suspend fun softDeleteFowl(fowlId: String, timestamp: Long)
    
    @Query("DELETE FROM fowls WHERE id = :fowlId")
    suspend fun hardDeleteFowl(fowlId: String)
    
    @Query("SELECT * FROM fowls WHERE needs_sync = 1")
    suspend fun getFowlsNeedingSync(): List<EnhancedFowlEntity>
    
    @Query("UPDATE fowls SET needs_sync = 0 WHERE id = :fowlId")
    suspend fun markFowlAsSynced(fowlId: String)
    
    @Query("SELECT COUNT(*) FROM fowls WHERE is_deleted = 0")
    suspend fun getFowlCount(): Int
    
    @Query("DELETE FROM fowls WHERE updated_at < :cutoffTime AND is_deleted = 1")
    suspend fun cleanupOldDeletedFowls(cutoffTime: Long)
}

/**
 * Farm DAO with offline support
 */
@Dao
interface FarmDao {
    
    @Query("SELECT * FROM farms WHERE is_deleted = 0 ORDER BY updated_at DESC")
    fun getAllFarms(): Flow<List<FarmEntity>>
    
    @Query("SELECT * FROM farms WHERE owner_id = :ownerId AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getFarmsByOwner(ownerId: String): Flow<List<FarmEntity>>
    
    @Query("SELECT * FROM farms WHERE id = :farmId AND is_deleted = 0")
    suspend fun getFarmById(farmId: String): FarmEntity?
    
    @Query("SELECT * FROM farms WHERE name LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%' AND is_deleted = 0")
    fun searchFarms(query: String): Flow<List<FarmEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarm(farm: FarmEntity)
    
    @Update
    suspend fun updateFarm(farm: FarmEntity)
    
    @Query("UPDATE farms SET is_deleted = 1, updated_at = :timestamp WHERE id = :farmId")
    suspend fun softDeleteFarm(farmId: String, timestamp: Long)
    
    @Query("DELETE FROM farms WHERE id = :farmId")
    suspend fun hardDeleteFarm(farmId: String)
    
    @Query("SELECT * FROM farms WHERE needs_sync = 1")
    suspend fun getFarmsNeedingSync(): List<FarmEntity>
    
    @Query("UPDATE farms SET needs_sync = 0 WHERE id = :farmId")
    suspend fun markFarmAsSynced(farmId: String)
    
    @Query("SELECT COUNT(*) FROM farms WHERE is_deleted = 0")
    suspend fun getFarmCount(): Int
    
    @Query("DELETE FROM farms WHERE updated_at < :cutoffTime AND is_deleted = 1")
    suspend fun cleanupOldDeletedFarms(cutoffTime: Long)
}

/**
 * Type converters for complex data types
 */
class Converters {
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }
    
    @TypeConverter
    fun fromMap(value: Map<String, String>): String {
        return value.entries.joinToString(";") { "${it.key}:${it.value}" }
    }
    
    @TypeConverter
    fun toMap(value: String): Map<String, String> {
        return if (value.isEmpty()) {
            emptyMap()
        } else {
            value.split(";").associate {
                val parts = it.split(":")
                parts[0] to (parts.getOrNull(1) ?: "")
            }
        }
    }
}

@Dao
interface EnhancedUserDao {
}

@Dao
interface SyncDao {
}

@Dao
interface OfflineActionDao {
}

@Dao
interface CachedImageDao {
}

@Dao
interface NotificationDao {
}

@Dao
interface TransactionDao {
}

@Dao
interface TraceabilityDao {
}