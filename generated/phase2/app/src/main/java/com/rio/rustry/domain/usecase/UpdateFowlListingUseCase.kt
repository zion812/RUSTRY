// generated/phase2/app/src/main/java/com/rio/rustry/domain/usecase/UpdateFowlListingUseCase.kt

package com.rio.rustry.domain.usecase

import com.rio.rustry.listing.FowlListingData
import com.rio.rustry.listing.ListingRepository
import javax.inject.Inject

class UpdateFowlListingUseCase @Inject constructor(
    private val listingRepository: ListingRepository
) {
    suspend operator fun invoke(fowlId: String, listingData: FowlListingData): Result<Unit> {
        return listingRepository.updateListing(fowlId, listingData)
    }
}