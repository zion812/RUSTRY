package com.rio.rustry.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.rio.rustry.data.local.dao.FowlDao
import com.rio.rustry.data.local.entity.FowlEntity
import com.rio.rustry.data.local.entity.FavoriteEntity
import com.rio.rustry.data.local.entity.PendingChangeEntity
import com.rio.rustry.data.local.entity.Converters

@Database(
    entities = [
        FowlEntity::class,
        FavoriteEntity::class,
        PendingChangeEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RustryDatabase : RoomDatabase() {

    abstract fun fowlDao(): FowlDao

    companion object {
        private const val DATABASE_NAME = "rustry_database"

        @Volatile
        private var INSTANCE: RustryDatabase? = null

        fun getDatabase(context: Context): RustryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RustryDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration() // For development only
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Migration from version 1 to 2 (example for future use)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example migration - add new column
                // database.execSQL("ALTER TABLE fowls ADD COLUMN newColumn TEXT")
            }
        }
    }
}