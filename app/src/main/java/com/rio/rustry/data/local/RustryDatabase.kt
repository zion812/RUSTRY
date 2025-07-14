package com.rio.rustry.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rio.rustry.data.local.entity.*

/**
 * Main Room Database for RUSTRY
 */
@Database(
    entities = [
        EnhancedFowlEntity::class,
        FarmEntity::class,
        FlockEntity::class,
        HealthRecordEntity::class,
        SaleEntity::class,
        InventoryEntity::class,
        ChangeLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RustryDatabase : RoomDatabase() {
    
    abstract fun fowlDao(): FowlDao
    abstract fun farmDao(): FarmDao
    
    companion object {
        const val DATABASE_NAME = "rustry_database"
    }
}

/**
 * Type converters for complex data types
 */
class Converters {
    
    @androidx.room.TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }
    
    @androidx.room.TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }
    
    @androidx.room.TypeConverter
    fun fromMap(value: Map<String, String>): String {
        return value.entries.joinToString(";") { "${it.key}:${it.value}" }
    }
    
    @androidx.room.TypeConverter
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

/**
 * Farm DAO interface
 */
@androidx.room.Dao
interface FarmDao {
    
    @androidx.room.Query("SELECT * FROM farms WHERE is_deleted = 0 ORDER BY updated_at DESC")
    fun getAllFarms(): kotlinx.coroutines.flow.Flow<List<FarmEntity>>
    
    @androidx.room.Query("SELECT * FROM farms WHERE owner_id = :ownerId AND is_deleted = 0")
    fun getFarmsByOwner(ownerId: String): kotlinx.coroutines.flow.Flow<List<FarmEntity>>
    
    @androidx.room.Query("SELECT * FROM farms WHERE id = :farmId AND is_deleted = 0")
    suspend fun getFarmById(farmId: String): FarmEntity?
    
    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertFarm(farm: FarmEntity)
    
    @androidx.room.Update
    suspend fun updateFarm(farm: FarmEntity)
    
    @androidx.room.Delete
    suspend fun deleteFarm(farm: FarmEntity)
}