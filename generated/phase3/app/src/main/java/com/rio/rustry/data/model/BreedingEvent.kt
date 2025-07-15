// generated/phase3/app/src/main/java/com/rio/rustry/data/model/BreedingEvent.kt

package com.rio.rustry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "breeding_events")
data class BreedingEvent(
    @PrimaryKey val id: String = "",
    val sireId: String = "",
    val damId: String = "",
    val offspringIds: List<String> = emptyList(),
    val breedingDate: Long = 0L,
    val expectedHatchDate: Long = 0L,
    val actualHatchDate: Long? = null,
    val eggCount: Int = 0,
    val hatchedCount: Int = 0,
    val notes: String = ""
)