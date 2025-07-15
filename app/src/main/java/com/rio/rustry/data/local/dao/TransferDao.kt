// generated/phase3/app/src/main/java/com/rio/rustry/data/local/dao/TransferDao.kt

package com.rio.rustry.data.local.dao

import androidx.room.*
import com.rio.rustry.data.model.Transfer
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {
    
    @Query("SELECT * FROM transfers ORDER BY timestamp DESC")
    fun getAllTransfers(): Flow<List<Transfer>>
    
    @Query("SELECT * FROM transfers WHERE id = :transferId")
    suspend fun getTransferById(transferId: String): Transfer?
    
    @Query("SELECT * FROM transfers WHERE fromUid = :userId OR toUid = :userId ORDER BY timestamp DESC")
    suspend fun getUserTransfers(userId: String): List<Transfer>
    
    @Query("SELECT * FROM transfers WHERE status = :status ORDER BY timestamp DESC")
    suspend fun getTransfersByStatus(status: String): List<Transfer>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: Transfer)
    
    @Update
    suspend fun updateTransfer(transfer: Transfer)
    
    @Delete
    suspend fun deleteTransfer(transfer: Transfer)
    
    @Query("DELETE FROM transfers WHERE id = :transferId")
    suspend fun deleteTransferById(transferId: String)
    
    @Query("UPDATE transfers SET status = :status WHERE id = :transferId")
    suspend fun updateTransferStatus(transferId: String, status: String)
    
    @Query("UPDATE transfers SET verified = :verified WHERE id = :transferId")
    suspend fun updateTransferVerification(transferId: String, verified: Boolean)
}