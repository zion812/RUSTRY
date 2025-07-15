// generated/phase3/app/src/main/java/com/rio/rustry/data/model/Transfer.kt

package com.rio.rustry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transfers")
data class Transfer(
    @PrimaryKey val id: String = "",
    val fromUid: String = "",
    val toUid: String = "",
    val fowlId: String = "",
    val status: TransferStatus = TransferStatus.PENDING,
    val proofUrls: List<String> = emptyList(),
    val timestamp: Long = 0L,
    val nfcData: String? = null,
    val photoUrl: String? = null,
    val signature: String? = null,
    val verified: Boolean = false
)