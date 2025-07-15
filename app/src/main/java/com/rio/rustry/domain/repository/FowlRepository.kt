// app/src/main/java/com/rio/rustry/domain/repository/FowlRepository.kt

package com.rio.rustry.domain.repository

import com.rio.rustry.data.model.Fowl
import com.rio.rustry.promotions.BoostDuration

interface FowlRepository {
    suspend fun getUserFowls(): List<Fowl>
    suspend fun getFowlById(fowlId: String): Fowl?
    suspend fun boostListing(fowlId: String, duration: BoostDuration): Boolean
    suspend fun updateFowl(fowl: Fowl)
    suspend fun deleteFowl(fowlId: String)
}