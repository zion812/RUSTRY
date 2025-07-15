// generated/phase2/app/src/main/java/com/rio/rustry/data/model/CartItem.kt

package com.rio.rustry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey
    val fowlId: String = "",
    val fowlName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val userId: String = ""
)