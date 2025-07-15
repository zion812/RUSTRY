// generated/phase3/app/src/main/java/com/rio/rustry/domain/repository/BreedingRepository.kt

package com.rio.rustry.domain.repository

import com.rio.rustry.breeding.FamilyTree
import com.rio.rustry.breeding.VaccinationEvent
import com.rio.rustry.dashboard.AnalyticsPeriod
import com.rio.rustry.dashboard.BreedingAnalytics

interface BreedingRepository {
    suspend fun getBreedingAnalytics(startDate: Long, endDate: Long, period: AnalyticsPeriod): BreedingAnalytics
    suspend fun getFamilyTree(fowlId: String): FamilyTree
    suspend fun getVaccinationEvents(fowlId: String): List<VaccinationEvent>
    suspend fun addVaccinationEvent(fowlId: String, event: VaccinationEvent)
    suspend fun updateVaccinationEvent(event: VaccinationEvent)
    suspend fun deleteVaccinationEvent(eventId: String)
    suspend fun markVaccinationComplete(eventId: String)
}