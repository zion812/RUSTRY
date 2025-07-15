// generated/phase2/app/src/main/java/com/rio/rustry/data/local/dao/CartDao.kt

package com.rio.rustry.data.local.dao

import androidx.room.*
import com.rio.rustry.data.model.CartItem

@Dao
interface CartDao {
    
    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    suspend fun getCartItems(userId: String): List<CartItem>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem)
    
    @Query("UPDATE cart_items SET quantity = :quantity WHERE fowlId = :fowlId AND userId = :userId")
    suspend fun updateQuantity(fowlId: String, userId: String, quantity: Int)
    
    @Query("DELETE FROM cart_items WHERE fowlId = :fowlId AND userId = :userId")
    suspend fun removeCartItem(fowlId: String, userId: String)
    
    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: String)
}