// generated/phase2/app/src/main/java/com/rio/rustry/domain/usecase/AddToCartUseCase.kt

package com.rio.rustry.domain.usecase

import com.rio.rustry.orders.OrderRepository
import javax.inject.Inject

class AddToCartUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(fowlId: String, quantity: Int = 1): Result<Unit> {
        return orderRepository.addToCart(fowlId, quantity)
    }
}