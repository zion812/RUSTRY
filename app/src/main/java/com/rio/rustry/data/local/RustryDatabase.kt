package com.rio.rustry.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.rio.rustry.data.local.Converters
import com.rio.rustry.data.local.entity.*

/**
 * Room database for offline caching and data persistence
 * 
 * Features:
 * - Comprehensive entity coverage
 * - Type converters for complex data types
 * - Migration support
 * - Multi-instance invalidation
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
        UserFavoriteEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RustryDatabase : RoomDatabase() {
    
    abstract fun fowlDao(): FowlDao
    
    companion object {
        const val DATABASE_NAME = "rustry_database"
        
        @Volatile
        private var INSTANCE: RustryDatabase? = null
        
        fun getDatabase(context: Context): RustryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RustryDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .enableMultiInstanceInvalidation()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}