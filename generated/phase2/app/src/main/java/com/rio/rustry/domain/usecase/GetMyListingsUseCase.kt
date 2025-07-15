// generated/phase2/app/src/main/java/com/rio/rustry/domain/usecase/GetMyListingsUseCase.kt

package com.rio.rustry.domain.usecase

import com.rio.rustry.data.model.Fowl
import com.rio.rustry.listing.ListingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMyListingsUseCase @Inject constructor(
    private val listingRepository: ListingRepository
) {
    suspend operator fun invoke(): Flow<Result<List<Fowl>>> {
        return listingRepository.getMyListings()
    }
}