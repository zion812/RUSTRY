// generated/phase3/app/src/main/java/com/rio/rustry/data/model/VaccinationEvent.kt

package com.rio.rustry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vaccination_events")
data class VaccinationEvent(
    @PrimaryKey val id: String = "",
    val fowlId: String = "",
    val vaccineName: String = "",
    val scheduledDate: Long = 0L,
    val completedDate: Long? = null,
    val status: VaccinationStatus = VaccinationStatus.PENDING,
    val notes: String = "",
    val reminderSet: Boolean = false
)

enum class VaccinationStatus {
    PENDING,
    COMPLETED,
    OVERDUE
}