// generated/phase2/app/src/main/java/com/rio/rustry/domain/usecase/UpdateCartQuantityUseCase.kt

package com.rio.rustry.domain.usecase

import com.rio.rustry.orders.OrderRepository
import javax.inject.Inject

class UpdateCartQuantityUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(fowlId: String, quantity: Int): Result<Unit> {
        return orderRepository.updateCartQuantity(fowlId, quantity)
    }
}