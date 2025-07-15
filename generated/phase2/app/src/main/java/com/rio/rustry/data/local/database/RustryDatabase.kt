// generated/phase2/app/src/main/java/com/rio/rustry/data/local/database/RustryDatabase.kt

package com.rio.rustry.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rio.rustry.data.local.dao.CartDao
import com.rio.rustry.data.local.dao.ChatMessageDao
import com.rio.rustry.data.local.dao.FowlDao
import com.rio.rustry.data.model.CartItem
import com.rio.rustry.data.model.ChatMessage
import com.rio.rustry.data.model.Fowl

@Database(
    entities = [
        Fowl::class,
        ChatMessage::class,
        CartItem::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RustryDatabase : RoomDatabase() {
    abstract fun fowlDao(): FowlDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun cartDao(): CartDao
}