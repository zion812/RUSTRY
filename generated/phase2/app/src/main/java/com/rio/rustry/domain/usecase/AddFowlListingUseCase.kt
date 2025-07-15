// generated/phase2/app/src/main/java/com/rio/rustry/domain/usecase/AddFowlListingUseCase.kt

package com.rio.rustry.domain.usecase

import com.rio.rustry.listing.FowlListingData
import com.rio.rustry.listing.ListingRepository
import javax.inject.Inject

class AddFowlListingUseCase @Inject constructor(
    private val listingRepository: ListingRepository
) {
    suspend operator fun invoke(listingData: FowlListingData): Result<String> {
        return listingRepository.addListing(listingData)
    }
}