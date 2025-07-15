// generated/phase2/app/src/main/java/com/rio/rustry/domain/usecase/RemoveFromCartUseCase.kt

package com.rio.rustry.domain.usecase

import com.rio.rustry.orders.OrderRepository
import javax.inject.Inject

class RemoveFromCartUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(fowlId: String): Result<Unit> {
        return orderRepository.removeFromCart(fowlId)
    }
}